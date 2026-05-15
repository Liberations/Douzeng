package com.aweme.hook;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Xposed 模块 - 修改抖音离线视频缓存数量限制
 * 
 * 核心 Hook 点分析（基于 smali 反编译）:
 * 
 * 1. OfflineKevaUtils.LJFF() - 读取最大缓存数量的核心方法
 *    - 从 Keva 存储读取 "cache_count" 键
 *    - 默认值为 50 或 100（取决于实验开关 OfflineModeLiteExp）
 * 
 * 2. OfflineKevaUtils.LJIILIIL() - 读取真实缓存计数
 *    - 从 Keva 存储读取 "true_cache_count" 键
 *    - 默认值为 100
 * 
 * 3. OfflineKevaUtils.LJIIJ() - 读取本地视频数量
 *    - 从 Keva 存储读取 "local_video_count" 键
 * 
 * 4. OfflineModeSettingDialog 构造函数 - UI 层缓存选项常量
 *    - mCacheCount100 = 100 (0x64)
 *    - mCacheCount150 = 150 (0x96)
 *    - mCacheCount200 = 200 (0xc8)
 * 
 * 5. OfflineModeSettingDialog.initView() - 初始化 UI 显示
 *    - 设置文本 "50条"/"100条" 等
 * 
 * 存储机制:
 * - 使用 Keva (类似 SharedPreferences) 存储配置
 * - 键名: "cache_count", "true_cache_count", "local_video_count", "user_enable" 等
 */
public class MyTest implements IXposedHookLoadPackage {
    private static final String TAG = "OfflineCacheHook";
    private static final String PREF_NAME = "OfflineCacheSettings";
    private static final String KEY_CACHE_COUNT = "cache_count";
    private static final String KEY_TRUE_CACHE_COUNT = "true_cache_count";
    private static final String KEY_USER_ENABLE = "user_enable";
    private static final int DEFAULT_CACHE_COUNT = 1000;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.d(TAG, "========================================");
        Log.d(TAG, "handleLoadPackage: " + lpparam.packageName);
        Log.d(TAG, "========================================");
        
        // Hook 抖音主进程
        if ("com.ss.android.ugc.aweme".equals(lpparam.packageName)) {
            Log.d(TAG, "检测到抖音应用，开始加载Hook");
            hookOfflineCacheCount(lpparam);
        }
    }

    /**
     * 获取用户设置的缓存数量
     * 优先通过ContentProvider读取（跨应用标准方案），其次XSharedPreferences，默认返回 1000
     */
    private int getCacheCount() {
        // 方法1: 通过ContentProvider.call()读取（标准Android跨应用方案）
        try {
            // 使用反射获取当前应用的Context
            android.content.Context ctx = (android.content.Context) 
                Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null);
            if (ctx == null) {
                // 备用方法
                ctx = (android.content.Context) 
                    Class.forName("android.app.ActivityThread")
                        .getMethod("systemMain")
                        .invoke(null);
            }
            
            if (ctx != null) {
                android.content.ContentResolver cr = ctx.getContentResolver();
                Uri uri = Uri.parse("content://com.aweme.hook.settings/settings");
                Bundle result = cr.call(uri, "getCacheCount", null, null);
                if (result != null && result.containsKey(KEY_CACHE_COUNT)) {
                    int count = result.getInt(KEY_CACHE_COUNT);
                    Log.d(TAG, "[成功] 从ContentProvider读取: " + count);
                    return count;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "[ContentProvider] 读取失败: " + e.getMessage());
        }
        
        // 方法2: XSharedPreferences（备用）
        try {
            XSharedPreferences prefs = new XSharedPreferences("com.wen.test", PREF_NAME);
            prefs.makeWorldReadable();
            prefs.reload();
            if (prefs.getAll() != null && prefs.getAll().containsKey(KEY_CACHE_COUNT)) {
                int count = prefs.getInt(KEY_CACHE_COUNT, DEFAULT_CACHE_COUNT);
                Log.d(TAG, "[成功] 从XSharedPreferences读取: " + count);
                return count;
            }
        } catch (Exception e) {
            Log.w(TAG, "[XSharedPreferences] 读取失败: " + e.getMessage());
        }
        
        Log.w(TAG, "[警告] 所有方法都失败，使用默认值: " + DEFAULT_CACHE_COUNT);
        return DEFAULT_CACHE_COUNT;
    }

    /**
     * Hook 离线缓存相关方法
     */
    private void hookOfflineCacheCount(XC_LoadPackage.LoadPackageParam lpparam) {
        Log.d(TAG, "开始 Hook 离线缓存模块");
        
        try {
            // Hook 1: OfflineKevaUtils.LJFF() - 核心缓存数量读取方法
            // 这是最重要的 Hook 点，控制最大缓存数量
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineKevaUtils",
                lpparam.classLoader,
                "LJFF",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        int cacheCount = getCacheCount();
                        Log.d(TAG, "[Hook1] OfflineKevaUtils.LJFF() 返回: " + cacheCount);
                        return cacheCount;
                    }
                }
            );

            // Hook 2: OfflineKevaUtils.LJIILIIL() - 真实缓存计数
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineKevaUtils",
                lpparam.classLoader,
                "LJIILIIL",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        int trueCount = getCacheCount();
                        Log.d(TAG, "[Hook2] OfflineKevaUtils.LJIILIIL() 返回: " + trueCount);
                        return trueCount;
                    }
                }
            );

            // Hook 3: OfflineKevaUtils.LJIIJ() - 本地视频数量
            // 返回一个较小的值，让应用认为还有很多缓存空间
//            XposedHelpers.findAndHookMethod(
//                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineKevaUtils",
//                lpparam.classLoader,
//                "LJIIJ",
//                new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        int originalCount = (int) param.getResult();
//                        // 返回实际数量的 1/10，让应用认为还有很多空间
//                        int fakeCount = Math.max(0, originalCount / 10);
//                        param.setResult(fakeCount);
//                        Log.d(TAG, "[Hook3] OfflineKevaUtils.LJIIJ() 原始: " + originalCount + " -> 伪造: " + fakeCount);
//                    }
//                }
//            );

            // Hook 4: OfflineKevaUtils.LJ() - 缓存按钮状态
            // 返回一个正常状态值，避免按钮被禁用
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineKevaUtils",
                lpparam.classLoader,
                "LJ",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int originalStatus = (int) param.getResult();
                        // 确保返回非 -1 值，表示缓存功能正常
                        if (originalStatus == -1) {
                            param.setResult(0);
                            Log.d(TAG, "[Hook4] OfflineKevaUtils.LJ() 修复状态: -1 -> 0");
                        }
                    }
                }
            );

            // Hook 5: OfflineKevaUtils.LJIILL() - 用户启用状态
            // 确保离线模式始终启用
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineKevaUtils",
                lpparam.classLoader,
                "LJIILL",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int enable = (int) param.getResult();
                        if (enable == 0) {
                            param.setResult(1);
                            Log.d(TAG, "[Hook5] OfflineKevaUtils.LJIILL() 启用离线模式");
                        }
                    }
                }
            );

            // Hook 6: OfflineKevaUtils.LJI() - 缓存大小
            // 返回较小的缓存大小，让应用认为还有很多空间
//            XposedHelpers.findAndHookMethod(
//                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineKevaUtils",
//                lpparam.classLoader,
//                "LJI",
//                new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        long originalSize = (long) param.getResult();
//                        // 返回实际大小的 1/10
//                        long fakeSize = originalSize / 10;
//                        param.setResult(fakeSize);
//                        Log.d(TAG, "[Hook6] OfflineKevaUtils.LJI() 原始: " + originalSize + " -> 伪造: " + fakeSize);
//                    }
//                }
//            );

            // Hook 7: OfflineKevaUtils.LJIIIZ() - 首次使用用户标记
            // 返回 false，避免某些限制
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineKevaUtils",
                lpparam.classLoader,
                "LJIIIZ",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        Log.d(TAG, "[Hook7] OfflineKevaUtils.LJIIIZ() 返回 false");
                        return false;
                    }
                }
            );


            // Hook 9: OfflineModeSettingDialog.initView() - 修改 UI 文本显示
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineModeSettingDialog",
                lpparam.classLoader,
                "initView",
                android.view.View.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int cacheCount = getCacheCount();
                        Object dialog = param.thisObject;
                        
                        try {
                            // 获取TextView字段（类型是DmtTextView）
                            Object mText100 = XposedHelpers.getObjectField(dialog, "mText100");
                            Object mText150 = XposedHelpers.getObjectField(dialog, "mText150");
                            Object mText200 = XposedHelpers.getObjectField(dialog, "mText200");
                            
                            String text = cacheCount + "条";
                            
                            if (mText100 != null) {
                                XposedHelpers.callMethod(mText100, "setText", text);
                                Log.d(TAG, "[Hook9] 修改mText100文本: " + text);
                            }
                            if (mText150 != null) {
                                XposedHelpers.callMethod(mText150, "setText", text);
                                Log.d(TAG, "[Hook9] 修改mText150文本: " + text);
                            }
                            if (mText200 != null) {
                                XposedHelpers.callMethod(mText200, "setText", text);
                                Log.d(TAG, "[Hook9] 修改mText200文本: " + text);
                            }
                            
                            Log.d(TAG, "[Hook9] OfflineModeSettingDialog initView() 完成 - UI文本: " + text);
                        } catch (Exception e) {
                            Log.e(TAG, "[Hook9] 修改 UI 文本失败", e);
                        }
                    }
                }
            );

            // Hook 10: OfflineModeCacheVideoManager.LIZ() - 缓存管理器单例
            // 这里可以添加更多自定义逻辑
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineModeCacheVideoManager",
                lpparam.classLoader,
                "LIZ",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Log.d(TAG, "[Hook10] OfflineModeCacheVideoManager.LIZ() 被调用");
                    }
                }
            );

            // Hook 11: OfflineModeDownloadOptPanel.initView() - 修改新版UI的缓存选项文本
            // 这个类使用 count50/count100/count150/count200 字段
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineModeDownloadOptPanel",
                lpparam.classLoader,
                "initView",
                android.view.View.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int cacheCount = getCacheCount();
                        Object panel = param.thisObject;
                        String text = cacheCount + "条";
                        
                        try {
                            // 尝试修改 count50/count100/count150/count200 字段
                            String[] fieldNames = {"count50", "count100", "count150", "count200"};
                            for (String fieldName : fieldNames) {
                                Object viewObj = XposedHelpers.getObjectField(panel, fieldName);
                                if (viewObj instanceof android.widget.TextView) {
                                    android.widget.TextView textView = (android.widget.TextView) viewObj;
                                    textView.setText(text);
                                    Log.d(TAG, "[Hook11] 修改" + fieldName + "文本: " + text);
                                }
                            }
                            Log.d(TAG, "[Hook11] OfflineModeDownloadOptPanel initView() 完成 - UI文本: " + text);
                        } catch (Exception e) {
                            Log.e(TAG, "[Hook11] 修改 OfflineModeDownloadOptPanel UI 文本失败", e);
                        }
                    }
                }
            );

            // Hook 12: OfflineModeDownloadOptPanel.updateCacheCountSelection() - 拦截选中状态更新
            // 这个方法会根据当前选中的值设置颜色，我们需要确保显示正确的文本
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineModeDownloadOptPanel",
                lpparam.classLoader,
                "updateCacheCountSelection",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int cacheCount = getCacheCount();
                        Object panel = param.thisObject;
                        String text = cacheCount + "条";
                        
                        try {
                            // 重新设置所有选项的文本
                            String[] fieldNames = {"count50", "count100", "count150", "count200"};
                            for (String fieldName : fieldNames) {
                                Object viewObj = XposedHelpers.getObjectField(panel, fieldName);
                                if (viewObj instanceof android.widget.TextView) {
                                    android.widget.TextView textView = (android.widget.TextView) viewObj;
                                    textView.setText(text);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "[Hook12] updateCacheCountSelection 后修改文本失败", e);
                        }
                    }
                }
            );

            // Hook 13: OfflineModeDownloadOptPanel.showAdjustCacheCountDialog() - 拦截调整缓存上限对话框
            // 修改对话框中显示的数量为用户设置的值
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineModeDownloadOptPanel",
                lpparam.classLoader,
                "showAdjustCacheCountDialog",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        int cacheCount = getCacheCount();
                        // 替换传入的参数为用户设置的值
                        param.args[0] = cacheCount;
                        Log.d(TAG, "[Hook13] showAdjustCacheCountDialog 修改参数: " + param.args[0] + " -> " + cacheCount);
                    }
                }
            );

            // Hook 14: OfflineModeDownloadOptPanel.handleCacheCountClick() - 拦截缓存数量点击事件
            // 确保点击时传递正确的数量值
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineModeDownloadOptPanel",
                lpparam.classLoader,
                "handleCacheCountClick",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        int cacheCount = getCacheCount();
                        // 替换传入的参数为用户设置的值
                        int originalValue = (int) param.args[0];
                        param.args[0] = cacheCount;
                        Log.d(TAG, "[Hook14] handleCacheCountClick 修改参数: " + originalValue + " -> " + cacheCount);
                    }
                }
            );

            // Hook 15: OfflineModeCacheVideoManager.LJIILL() - 缓存状态检查
            // 这个方法返回当前缓存状态：1=启用，2=暂停，3=完成，4=恢复中
            // 确保返回正确的状态，避免显示"缓存已暂停"
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineModeCacheVideoManager",
                lpparam.classLoader,
                "LJIILL",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int originalStatus = (int) param.getResult();
                        // 如果状态是2(暂停)或4(异常)，改为1(启用)
                        if (originalStatus == 2 || originalStatus == 4) {
                            param.setResult(1);
                            Log.d(TAG, "[Hook15] OfflineModeCacheVideoManager.LJIILL() 修复状态: " + originalStatus + " -> 1");
                        } else {
                            Log.d(TAG, "[Hook15] OfflineModeCacheVideoManager.LJIILL() 返回: " + originalStatus);
                        }
                    }
                }
            );

            // Hook 16: CacheEntranceViewHolder.m1() - 拦截缓存按钮点击事件
            // 确保点击按钮时能正确跳转到缓存设置页
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.download_center.adapter.CacheEntranceViewHolder",
                lpparam.classLoader,
                "m1",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Log.d(TAG, "[Hook16] CacheEntranceViewHolder.m1() 点击事件触发");
                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Log.d(TAG, "[Hook16] CacheEntranceViewHolder.m1() 点击事件完成");
                    }
                }
            );

            // Hook 17: OfflineKevaUtils.LJIIL() - 拦截缓存列表读取
            // 确保返回正确的缓存列表，触发缓存逻辑
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineKevaUtils",
                lpparam.classLoader,
                "LJIIL",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object result = param.getResult();
                        Log.d(TAG, "[Hook17] OfflineKevaUtils.LJIIL() 返回: " + (result != null ? result.getClass().getSimpleName() : "null"));
                    }
                }
            );

            // Hook 18: 抖音主Activity onCreate后自动触发缓存
            XposedHelpers.findAndHookMethod(
                "com.ss.android.ugc.aweme.main.MainActivity",
                lpparam.classLoader,
                "onCreate",
                android.os.Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Log.d(TAG, "[Hook18] 抖音主Activity已创建，准备自动触发缓存");
                        
                        // 延迟执行，确保UI已完全加载
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // 获取缓存管理器Class对象
                                    Class<?> cacheManagerClass = XposedHelpers.findClass(
                                        "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineModeCacheVideoManager",
                                        lpparam.classLoader
                                    );
                                    
                                    // 获取缓存管理器单例
                                    Object cacheManager = XposedHelpers.callStaticMethod(cacheManagerClass, "LIZ");
                                    
                                    if (cacheManager != null) {
                                        // 获取当前状态
                                        int currentStatus = (int) XposedHelpers.callMethod(cacheManager, "LJIILL");
                                        Log.d(TAG, "[Hook18] 当前缓存状态: " + currentStatus);
                                        
                                        // 如果状态不是1（启用），先设置为1
                                        if (currentStatus != 1) {
                                            XposedHelpers.callMethod(cacheManager, "LJIIIZ", 1);
                                            Log.d(TAG, "[Hook18] 设置缓存状态为启用");
                                        }
                                        
                                        // 尝试调用LJJ()开始缓存
                                        try {
                                            XposedHelpers.callMethod(cacheManager, "LJJ");
                                            Log.d(TAG, "[Hook18] 成功调用LJJ()触发缓存");
                                        } catch (Exception e) {
                                            Log.w(TAG, "[Hook18] LJJ()调用失败: " + e.getMessage());
                                        }
                                        
                                        // 尝试其他可能的方法
                                        try {
                                            // LJ()可能是启动缓存的方法
                                            XposedHelpers.callMethod(cacheManager, "LJ");
                                            Log.d(TAG, "[Hook18] 成功调用LJ()");
                                        } catch (Exception e) {
                                            Log.w(TAG, "[Hook18] LJ()调用失败: " + e.getMessage());
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "[Hook18] 自动触发缓存失败: " + e.getMessage(), e);
                                }
                            }
                        }, 3000); // 延迟3秒执行
                    }
                }
            );

            // Hook 19: 视频预加载增强 - 修改VideoPreloadManager预加载大小
            // 基于 dou2/ss/android/excitingvideo/video/VideoPreloadManager.java 分析
            try {
                // Hook getVideoPreloadSize() 方法，增大预加载大小
                XposedHelpers.findAndHookMethod(
                    "com.ss.android.excitingvideo.video.VideoPreloadManager",
                    lpparam.classLoader,
                    "getVideoPreloadSize",
                    "com.ss.android.excitingvideo.model.VideoAd",
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            int originalSize = (int) param.getResult();
                            // 将预加载大小增加到原来的3倍，最多50MB
                            int newSize = Math.min(originalSize * 3, 50 * 1024 * 1024);
                            param.setResult(newSize);
                            Log.d(TAG, "[Hook19] VideoPreloadManager 预加载大小: " + 
                                (originalSize / 1024 / 1024) + "MB -> " + (newSize / 1024 / 1024) + "MB");
                        }
                    }
                );
                
                // Hook preloadVideo() 方法，确保总是触发预加载
                XposedHelpers.findAndHookMethod(
                    "com.ss.android.excitingvideo.video.VideoPreloadManager",
                    lpparam.classLoader,
                    "preloadVideo",
                    "com.ss.android.excitingvideo.model.VideoCacheModel",
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Log.d(TAG, "[Hook19] VideoPreloadManager.preloadVideo() 被调用");
                        }
                    }
                );
                
                Log.d(TAG, "[成功] 视频预加载Hook已添加");
            } catch (Exception e) {
                Log.w(TAG, "[Hook19] VideoPreloadManager Hook失败: " + e.getMessage());
            }

            // Hook 20: 缓存命中统计 - 监控视频缓存效果
            try {
                // Hook TTVideoEngine.addTask() 方法，监控预加载任务
                XposedHelpers.findAndHookMethod(
                    "com.ss.ttvideoengine.TTVideoEngine",
                    lpparam.classLoader,
                    "addTask",
                    "com.ss.ttvideoengine.PreloaderItem",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Object task = param.args[0];
                            Log.d(TAG, "[Hook20] TTVideoEngine.addTask() 添加预加载任务: " + 
                                (task != null ? task.getClass().getSimpleName() : "null"));
                        }
                    }
                );
                
                Log.d(TAG, "[成功] 缓存统计Hook已添加");
            } catch (Exception e) {
                Log.w(TAG, "[Hook20] TTVideoEngine Hook失败: " + e.getMessage());
            }

            Log.d(TAG, "离线缓存 Hook 完成");
        } catch (Exception e) {
            Log.e(TAG, "Hook 离线缓存失败", e);
        }
    }
}
