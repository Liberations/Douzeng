package com.aweme.hook

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.xposed.bridge.event.YukiXposedEvent
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import org.luckypray.dexkit.DexKitBridge

@InjectYukiHookWithXposed(entryClassName = "DouZeng", isUsingResourcesHook = true)
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = YukiHookAPI.configs {
        debugLog {
            tag = "DouZeng"
            isEnable = true
            isRecord = false
            elements(TAG, PRIORITY, PACKAGE_NAME, USER_ID)
        }
        isDebug = BuildConfig.DEBUG
        isEnableHookSharedPreferences = true
    }

    override fun onHook() = YukiHookAPI.encase {
        try {
            System.loadLibrary("dexkit")
        } catch (_: Throwable) {
        }
        loadApp(name = "com.ss.android.ugc.aweme") {
            val bridge = runCatching { DexKitBridge.create(appInfo.sourceDir) }.getOrNull()

            // YukiHookAPI 内置 prefs（在 loadApp 作用域内可用）
            // 自动处理文件权限、路径等问题，比手动 XSharedPreferences 更可靠
            fun readCacheCount(): Int = runCatching {
                prefs("OfflineCacheSettings").getInt(KEY_CACHE_COUNT, DEFAULT_CACHE_COUNT)
            }.getOrElse { _ ->
                DEFAULT_CACHE_COUNT
            }

            val kevaUtilsName = bridge?.findClass {
                searchPackages("com.ss.android.ugc.aweme.feed.cache")
                matcher { usingStrings(listOf("cache_count", "true_cache_count")) }
            }?.singleOrNull()?.name

            android.util.Log.d("DouZeng", "[DexKit] keva=$kevaUtilsName")

            if (kevaUtilsName != null) {
                runCatching {
                    kevaUtilsName.toClass().resolve().apply {

                        // === 基于 DexKit 字符串匹配查找方法名（不依赖混淆名） ===
                        // 映射关系（基于 OfflineKevaUtils.java 反编译源码）：
                        // "cache_count"      → LJFF()       → replaceTo(自定义缓存数)
                        // "true_cache_count" → LJIILIIL()   → replaceTo(自定义缓存数)
                        // "cache_btn_status" → LJ()         → after: -1→0 (按钮状态修复)
                        // "user_enable"      → LJIILL()     → after: 0→1  (启用状态)

                        data class Target(val key: String, val action: String, val desc: String)

                        listOf(
                            Target("cache_count", "replace", "缓存数量"),
                            Target("true_cache_count", "replace", "真实缓存数"),
                            Target("cache_btn_status", "fixBtn", "按钮状态"),
                            Target("user_enable", "enable", "启用状态")
                        ).forEach { target ->
                            runCatching {
                                val foundMethods = bridge.findMethod {
                                    searchPackages("com.ss.android.ugc.aweme.feed.cache")
                                    matcher {
                                        usingStrings(listOf(target.key))
                                        returnType = "int"
                                        paramCount = 0
                                    }
                                }

                                if (foundMethods.isEmpty()) {
                                    android.util.Log.w(
                                        "DouZeng",
                                        "[DexKit] ⚠️ 未找到 '${target.key}' (${target.desc})"
                                    )
                                    return@forEach
                                }

                                val methodName =
                                    foundMethods.singleOrNull()?.name ?: foundMethods.single().name
                                android.util.Log.d(
                                    "DouZeng",
                                    "[DexKit] ${target.desc}: $methodName (key=${target.key})"
                                )

                                firstMethod {
                                    name = methodName
                                    emptyParameters()
                                    returnType = Int::class.java
                                }.hook {
                                    when (target.action) {
                                        "replace" -> replaceTo(any = readCacheCount())
                                        "fixBtn" -> after { if (result as? Int == -1) result = 0 }
                                        "enable" -> after { if (result as? Int == 0) result = 1 }
                                        else -> {}
                                    }
                                }
                                android.util.Log.d("DouZeng", "[Hook] ✅ ${target.desc} Hook 成功")
                            }.onFailure { e ->
                                android.util.Log.e(
                                    "DouZeng",
                                    "[Hook] ❌ ${target.desc}: ${e.message}"
                                )
                            }
                        }
                    }
                    android.util.Log.d("DouZeng", "[Hook] ✅ KevaUtils 全部完成")
                }.onFailure { e ->
                    android.util.Log.e("DouZeng", "[Hook] ❌ KevaUtils: ${e.message}")
                }
            }

            bridge?.close()

            // 直接使用已知完整类名（基于反编译源码确认）
            // OfflineModeDownloadOptPanel extends OfflineModeSettingDialog
            // 注意: updateCacheCountSelection / updateLayoutView 只在 DownloadOptPanel 中定义
            val downloadOptPanel =
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineModeDownloadOptPanel"
            val settingDialog =
                "com.ss.android.ugc.aweme.feed.cache.offlinemode.OfflineModeSettingDialog"

            runCatching {
                // === SettingDialog (父类) ===
                // 只有 initView + showAdjustCacheCountDialog
                settingDialog.toClass().resolve().apply {
                    firstMethod {
                        name = "initView"
                        parameters(android.view.View::class.java)
                    }.hook {
                        after {
                            modifyAllCacheTexts(instance, readCacheCount())
                            android.util.Log.d("DouZeng", "[Hook] ✅ $settingDialog#initView")
                        }
                    }
                    firstMethod {
                        name = "showAdjustCacheCountDialog"
                        parameters(Int::class.javaPrimitiveType!!)
                    }.hook {
                        before {
                            val originalValue = args().first()
                            val newCount = readCacheCount()
                            args().first().set(newCount)
                            android.util.Log.d(
                                "DouZeng",
                                "[Hook] showAdjustCacheCountDialog: $originalValue → $newCount"
                            )
                        }
                    }
                }
                android.util.Log.d("DouZeng", "[Hook] ✅ $settingDialog Hook 完成")
            }.onFailure { e ->
                android.util.Log.e("DouZeng", "[Hook] ❌ $settingDialog: ${e.message}")
            }

            runCatching {
                // === DownloadOptPanel (子类) ===
                // 包含全部方法: initView + updateCacheCountSelection + updateLayoutView + showAdjustCacheCountDialog
                downloadOptPanel.toClass().resolve().apply {
                    firstMethod {
                        name = "initView"
                        parameters(android.view.View::class.java)
                    }.hook {
                        after {
                            modifyAllCacheTexts(instance, readCacheCount())
                            android.util.Log.d("DouZeng", "[Hook] ✅ $downloadOptPanel#initView")
                        }
                    }
                    firstMethod {
                        name = "updateCacheCountSelection"
                        parameters(Int::class.javaPrimitiveType!!)
                    }.hook {
                        after { modifyAllCacheTexts(instance, readCacheCount()) }
                    }
                    firstMethod {
                        name = "updateLayoutView"
                        emptyParameters()
                    }.hook {
                        after { modifyAllCacheTexts(instance, readCacheCount()) }
                    }
                    firstMethod {
                        name = "showAdjustCacheCountDialog"
                        parameters(Int::class.javaPrimitiveType!!)
                    }.hook {
                        before {
                            val originalValue = args().first()
                            val newCount = readCacheCount()
                            args().first().set(newCount)
                            android.util.Log.d(
                                "DouZeng",
                                "[Hook] showAdjustCacheCountDialog: $originalValue → $newCount"
                            )
                        }
                    }
                }
                android.util.Log.d("DouZeng", "[Hook] ✅ $downloadOptPanel Hook 完成")
            }.onFailure { e ->
                android.util.Log.e("DouZeng", "[Hook] ❌ $downloadOptPanel: ${e.message}")
            }
        }
    }

    override fun onXposedEvent() {
        YukiXposedEvent.onHandleLoadPackage { run { } }
        YukiXposedEvent.onInitZygote { run { } }
    }

    /**
     * 修改所有缓存选项文本
     *
     * 基于 OfflineModeDownloadOptPanel.java 反编译源码:
     * - count50/count100/count150/count200 字段类型为 View (非 TextView)
     * - 在 isScene1Layout=false 模式下，这些 View 实际是 TextView
     * - 在 isScene1Layout=true 模式下，使用的是 DuxRadio (单选按钮)
     */
    private fun modifyAllCacheTexts(panelInstance: Any?, count: Int) {
        if (panelInstance == null) return
        val text = "${count}条"
        arrayOf("count50", "count100", "count150", "count200").forEach { fieldName ->
            runCatching {
                val field = panelInstance.javaClass.getDeclaredField(fieldName)
                field.isAccessible = true
                val viewObj = field.get(panelInstance)
                when (viewObj) {
                    is android.widget.TextView -> viewObj.text = text
                    is android.view.View -> {
                        // 对于普通 View，尝试通过 tag 或其他方式处理
                        // DuxRadio 场景下不修改文本（由系统管理选中状态）
                    }
                }
            }
        }
    }

    companion object {
        private const val KEY_CACHE_COUNT = "cache_count"
        private const val DEFAULT_CACHE_COUNT = 1000
    }
}
