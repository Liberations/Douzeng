package com.aweme.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import android.content.Context
import android.util.Log
import org.luckypray.dexkit.DexKitBridge

/**
 * Hook入口类 - 使用DexKit动态查找混淆后的类
 * 支持抖音升级后自动适配
 */
class HookEntry : IXposedHookLoadPackage {

    companion object {
        private const val TAG = "OfflineCacheHookKotlin"
        private const val KEY_CACHE_COUNT = "cache_count"
        
        // 尝试加载libdexkit.so
        init {
            try {
                System.loadLibrary("dexkit")
                Log.d(TAG, "libdexkit.so加载成功")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "libdexkit.so加载失败: ${e.message}")
            }
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam == null) return
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "handleLoadPackage: ${lpparam.packageName}")
        Log.d(TAG, "进程名: ${lpparam.processName}")
        Log.d(TAG, "========================================")
        
        if (lpparam.packageName != "com.ss.android.ugc.aweme") {
            return
        }

        Log.d(TAG, "检测到抖音应用，开始加载Hook")
        
        // 先尝试DexKit方式（自动适配混淆）
        try {
            Log.d(TAG, "尝试DexKit方式Hook...")
            val apkPath = lpparam.appInfo.sourceDir
            DexKitBridge.create(apkPath).use { bridge ->
                Log.d(TAG, "DexKitBridge创建成功")
                hookOfflineKevaUtils(bridge, lpparam)
                hookOfflineModeCacheVideoManager(bridge, lpparam)
                hookDownloadOptPanel(bridge, lpparam)
            }
            Log.d(TAG, "DexKit方式Hook完成")
        } catch (e: Throwable) {
            Log.e(TAG, "DexKit方式Hook失败: ${e.javaClass.simpleName} - ${e.message}", e)
            
            // 降级到传统方式
            Log.d(TAG, "降级到传统方式Hook...")
            try {
                hookOfflineKevaUtilsTraditional(lpparam)
                hookOfflineModeCacheVideoManagerTraditional(lpparam)
                Log.d(TAG, "传统方式Hook完成")
            } catch (e2: Throwable) {
                Log.e(TAG, "传统方式也失败了", e2)
            }
        }
    }

    /**
     * 使用DexKit动态查找并Hook OfflineKevaUtils
     */
    private fun hookOfflineKevaUtils(bridge: DexKitBridge, lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // 通过字符串特征查找类
            val kevaUtilsClass = findClassByStrings(
                bridge, 
                "com.ss.android.ugc.aweme.feed.cache",
                listOf("cache_count", "true_cache_count"),
                lpparam.classLoader
            )
            
            if (kevaUtilsClass == null) {
                Log.w(TAG, "[DexKit] 未找到OfflineKevaUtils类")
                return
            }

            Log.d(TAG, "[DexKit] 找到OfflineKevaUtils: ${kevaUtilsClass.name}")
            
            // Hook LJFF() - 注意cacheCount在方法执行时动态获取
            try {
                XposedHelpers.findAndHookMethod(kevaUtilsClass, "LJFF", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val cacheCount = getCacheCount()
                        param.result = cacheCount
                        Log.d(TAG, "[DexKit-Hook1] LJFF() -> $cacheCount")
                    }
                })
            } catch (e: NoSuchMethodError) {
                Log.w(TAG, "[DexKit] LJFF()不存在")
            }
            
            // Hook LJIILIIL()
            try {
                XposedHelpers.findAndHookMethod(kevaUtilsClass, "LJIILIIL", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val cacheCount = getCacheCount()
                        param.result = cacheCount
                        Log.d(TAG, "[DexKit-Hook2] LJIILIIL() -> $cacheCount")
                    }
                })
            } catch (e: NoSuchMethodError) {
                Log.w(TAG, "[DexKit] LJIILIIL()不存在")
            }
            
            // Hook LJ() - 按钮状态
            try {
                XposedHelpers.findAndHookMethod(kevaUtilsClass, "LJ", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val status = param.result as Int
                        if (status == -1) {
                            param.result = 0
                            Log.d(TAG, "[DexKit-Hook4] LJ() -1->0")
                        }
                    }
                })
            } catch (e: NoSuchMethodError) {
                Log.w(TAG, "[DexKit] LJ()不存在")
            }
            
            // Hook LJIILL() - 启用状态
            try {
                XposedHelpers.findAndHookMethod(kevaUtilsClass, "LJIILL", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val enable = param.result as Int
                        if (enable == 0) {
                            param.result = 1
                            Log.d(TAG, "[DexKit-Hook5] LJIILL() 启用")
                        }
                    }
                })
            } catch (e: NoSuchMethodError) {
                Log.w(TAG, "[DexKit] LJIILL()不存在")
            }
            
            Log.d(TAG, "[DexKit] OfflineKevaUtils Hook完成")
        } catch (e: Exception) {
            Log.e(TAG, "[DexKit] OfflineKevaUtils失败", e)
        }
    }

    /**
     * 使用DexKit动态查找并Hook OfflineModeCacheVideoManager
     */
    private fun hookOfflineModeCacheVideoManager(bridge: DexKitBridge, lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val managerClass = findClassByStrings(
                bridge,
                "com.ss.android.ugc.aweme.feed.cache",
                listOf("local_video_count", "user_enable"),
                lpparam.classLoader
            )
            
            if (managerClass == null) {
                Log.w(TAG, "[DexKit] 未找到OfflineModeCacheVideoManager类")
                return
            }

            Log.d(TAG, "[DexKit] 找到OfflineModeCacheVideoManager: ${managerClass.name}")

            // Hook LJIILL() - 状态码
            try {
                XposedHelpers.findAndHookMethod(managerClass, "LJIILL", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val status = param.result as Int
                        if (status == 2 || status == 4) {
                            param.result = 1
                            Log.d(TAG, "[DexKit-Hook15] LJIILL() $status->1")
                        }
                    }
                })
            } catch (e: NoSuchMethodError) {
                Log.w(TAG, "[DexKit] LJIILL()不存在")
            }
            
            Log.d(TAG, "[DexKit] OfflineModeCacheVideoManager Hook完成")
        } catch (e: Exception) {
            Log.e(TAG, "[DexKit] OfflineModeCacheVideoManager失败", e)
        }
    }

    /**
     * Hook UI面板的initView方法 - 修改缓存选项文本显示
     */
    private fun hookDownloadOptPanel(bridge: DexKitBridge, lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // 查找包含"条视频"字符串的UI类
            val panelClass = findClassByStrings(
                bridge,
                "com.ss.android.ugc.aweme.feed.cache",
                listOf("条视频", "50条", "100条"),
                lpparam.classLoader
            )
            
            if (panelClass == null) {
                Log.w(TAG, "[DexKit] 未找到OfflineModeDownloadOptPanel类")
                return
            }

            Log.d(TAG, "[DexKit] 找到UI面板类: ${panelClass.name}")

            // 使用 DexKit 查找所有方法
            try {
                DexKitBridge.create(lpparam.appInfo.sourceDir).use { bridge2 ->
                    // 查找参数为int的方法
                    val intMethods = bridge2.findMethod {
                        matcher {
                            declaredClass = panelClass.name
                            paramTypes = listOf("int")
                            returnType = "void"
                        }
                    }
                    
                    Log.d(TAG, "[DexKit] 找到${intMethods.size}个参数为int的方法")
                    
                    // 遍历所有int参数方法
                    for (method in intMethods) {
                        val methodName = method.name
                        Log.d(TAG, "[DexKit] 检查方法: $methodName")
                        
                        try {
                            val methodObj = panelClass.getDeclaredMethod(methodName, Int::class.java)
                            
                            when {
                                methodName.contains("Dialog", ignoreCase = true) || 
                                methodName.contains("Adjust", ignoreCase = true) -> {
                                    XposedHelpers.findAndHookMethod(
                                        panelClass, methodName, Int::class.java,
                                        object : XC_MethodHook() {
                                            override fun beforeHookedMethod(param: MethodHookParam) {
                                                val cacheCount = getCacheCount()
                                                val originalParam = param.args[0]
                                                param.args[0] = cacheCount
                                                Log.d(TAG, "[DexKit-Hook13] $methodName 修改参数: $originalParam -> $cacheCount")
                                            }
                                        }
                                    )
                                }
                                methodName.contains("Update", ignoreCase = true) || 
                                methodName.contains("Selection", ignoreCase = true) ||
                                methodName.contains("Count", ignoreCase = true) -> {
                                    XposedHelpers.findAndHookMethod(
                                        panelClass, methodName, Int::class.java,
                                        object : XC_MethodHook() {
                                            override fun afterHookedMethod(param: MethodHookParam) {
                                                val cacheCount = getCacheCount()
                                                val panel = param.thisObject
                                                val text = "${cacheCount}条"
                                                
                                                try {
                                                    val fieldNames = arrayOf("count50", "count100", "count150", "count200")
                                                    for (fieldName in fieldNames) {
                                                        try {
                                                            val viewObj = XposedHelpers.getObjectField(panel, fieldName)
                                                            if (viewObj is android.widget.TextView) {
                                                                viewObj.text = text
                                                            }
                                                        } catch (e: Exception) {}
                                                    }
                                                    Log.d(TAG, "[DexKit-Hook12] $methodName 后修改文本: $text")
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "[DexKit-Hook12] $methodName 修改文本失败", e)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            // 方法Hook失败，跳过
                        }
                    }
                    
                    // 查找参数为View的方法（initView）
                    val viewMethods = bridge2.findMethod {
                        matcher {
                            declaredClass = panelClass.name
                            paramTypes = listOf("android.view.View")
                            returnType = "void"
                        }
                    }
                    
                    Log.d(TAG, "[DexKit] 找到${viewMethods.size}个参数为View的方法")
                    
                    for (method in viewMethods) {
                        val methodName = method.name
                        Log.d(TAG, "[DexKit] 检查View方法: $methodName")
                        
                        try {
                            XposedHelpers.findAndHookMethod(
                                panelClass,
                                methodName,
                                android.view.View::class.java,
                                object : XC_MethodHook() {
                                    override fun afterHookedMethod(param: MethodHookParam) {
                                        val cacheCount = getCacheCount()
                                        val panel = param.thisObject
                                        val text = "${cacheCount}条"
                                        
                                        try {
                                            val fieldNames = arrayOf("count50", "count100", "count150", "count200")
                                            for (fieldName in fieldNames) {
                                                try {
                                                    val viewObj = XposedHelpers.getObjectField(panel, fieldName)
                                                    if (viewObj is android.widget.TextView) {
                                                        viewObj.text = text
                                                        Log.d(TAG, "[DexKit-Hook11] 修改${fieldName}文本: $text")
                                                    }
                                                } catch (e: Exception) {}
                                            }
                                            Log.d(TAG, "[DexKit-Hook11] $methodName 完成 - UI文本: $text")
                                        } catch (e: Exception) {
                                            Log.e(TAG, "[DexKit-Hook11] 修改UI文本失败", e)
                                        }
                                    }
                                }
                            )
                            break  // 找到第一个View参数的方法就Hook
                        } catch (e: Exception) {
                            // Hook失败，继续尝试下一个
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "[DexKit] 方法查找失败", e)
            }
            
            Log.d(TAG, "[DexKit] UI面板Hook完成")
        } catch (e: Exception) {
            Log.e(TAG, "[DexKit] UI面板Hook失败", e)
        }
    }

    /**
     * 通过字符串特征查找类
     */
    private fun findClassByStrings(
        bridge: DexKitBridge,
        searchPackage: String,
        strings: List<String>,
        classLoader: ClassLoader
    ): Class<*>? {
        return try {
            val result = bridge.findClass {
                searchPackages(searchPackage)
                matcher {
                    usingStrings = strings
                }
            }
            
            if (result.isNotEmpty()) {
                val className = result[0].name
                Log.d(TAG, "[DexKit] 找到类: $className")
                Class.forName(className, true, classLoader)
            } else {
                Log.w(TAG, "[DexKit] 未找到包含字符串${strings.joinToString()}的类")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "[DexKit] 查找类失败", e)
            null
        }
    }

    /**
     * 传统方式Hook（使用固定类名）
     */
    private fun hookOfflineKevaUtilsTraditional(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val className = "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineKevaUtils"
            val kevaClass = Class.forName(className, true, lpparam.classLoader)
            Log.d(TAG, "找到OfflineKevaUtils类: $className")
            
            // Hook LJFF() - 动态获取缓存数量
            try {
                XposedHelpers.findAndHookMethod(kevaClass, "LJFF", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val cacheCount = getCacheCount()
                        param.result = cacheCount
                        Log.d(TAG, "[传统-Hook1] LJFF() 返回: $cacheCount")
                    }
                })
            } catch (e: NoSuchMethodError) {
                Log.w(TAG, "[传统] LJFF()方法不存在")
            }
            
            // Hook LJIILIIL()
            try {
                XposedHelpers.findAndHookMethod(kevaClass, "LJIILIIL", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val cacheCount = getCacheCount()
                        param.result = cacheCount
                        Log.d(TAG, "[传统-Hook2] LJIILIIL() 返回: $cacheCount")
                    }
                })
            } catch (e: NoSuchMethodError) {
                Log.w(TAG, "[传统] LJIILIIL()方法不存在")
            }
            
            // Hook LJ() - 按钮状态
            try {
                XposedHelpers.findAndHookMethod(kevaClass, "LJ", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val status = param.result as Int
                        if (status == -1) {
                            param.result = 0
                            Log.d(TAG, "[传统-Hook4] LJ() 修复: -1 -> 0")
                        }
                    }
                })
            } catch (e: NoSuchMethodError) {
                Log.w(TAG, "[传统] LJ()方法不存在")
            }
            
            // Hook LJIILL() - 启用状态
            try {
                XposedHelpers.findAndHookMethod(kevaClass, "LJIILL", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val enable = param.result as Int
                        if (enable == 0) {
                            param.result = 1
                            Log.d(TAG, "[传统-Hook5] LJIILL() 启用")
                        }
                    }
                })
            } catch (e: NoSuchMethodError) {
                Log.w(TAG, "[传统] LJIILL()方法不存在")
            }
            
            Log.d(TAG, "传统OfflineKevaUtils Hook完成")
        } catch (e: Exception) {
            Log.e(TAG, "传统OfflineKevaUtils Hook失败", e)
        }
    }
    
    /**
     * 传统方式Hook OfflineModeCacheVideoManager
     */
    private fun hookOfflineModeCacheVideoManagerTraditional(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val className = "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineModeCacheVideoManager"
            val managerClass = Class.forName(className, true, lpparam.classLoader)
            Log.d(TAG, "找到OfflineModeCacheVideoManager类: $className")
            
            // Hook LJIILL() - 状态码 (2=暂停, 4=恢复中 -> 1=启用)
            try {
                XposedHelpers.findAndHookMethod(managerClass, "LJIILL", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val status = param.result as Int
                        if (status == 2 || status == 4) {
                            param.result = 1
                            Log.d(TAG, "[传统-Hook15] LJIILL() 修复: $status -> 1")
                        }
                    }
                })
            } catch (e: NoSuchMethodError) {
                Log.w(TAG, "[传统] LJIILL()方法不存在")
            }
            
            Log.d(TAG, "传统OfflineModeCacheVideoManager Hook完成")
        } catch (e: Exception) {
            Log.e(TAG, "传统OfflineModeCacheVideoManager Hook失败", e)
        }
    }

    /**
     * 获取用户设置的缓存数量
     * 通过 ContentProvider 读取
     */
    private fun getCacheCount(): Int {
        return try {
            // 使用反射获取当前应用的Context
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentApplicationMethod = activityThreadClass.getMethod("currentApplication")
            val currentApplication = currentApplicationMethod.invoke(null) as? Context
            
            if (currentApplication == null) {
                Log.w(TAG, "当前Application为null，使用默认值")
                return 1000
            }
            
            val resolver = currentApplication.contentResolver
            val uri = android.net.Uri.parse("content://com.aweme.hook.settings/settings")
            val bundle = resolver.call(uri, "getCacheCount", null, null)
            val count = bundle?.getInt("cache_count", 1000) ?: 1000
            Log.d(TAG, "从ContentProvider读取: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "读取缓存数量失败，使用默认值: 1000", e)
            1000
        }
    }
}
