package com.aweme.hook

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aweme.hook.databinding.ActivityMainBinding
import com.highcapable.yukihookapi.YukiHookAPI
import java.io.File

/**
 * 主界面 - 用于设置离线缓存数量
 * 该界面将设置保存到 SharedPreferences，Xposed 模块会读取这个值
 */
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val systemPaddingTop = systemBarsInsets.top
            val systemPaddingBottom = systemBarsInsets.bottom

            val customPaddingPx =
                resources.getDimensionPixelSize(R.dimen.padding_16) // 或直接用 dp 转 px
            view.setPadding(
                customPaddingPx,
                systemPaddingTop,
                customPaddingPx,
                systemPaddingBottom
            )
            // 消费掉 insets，避免系统再次应用默认的 fitsSystemWindows 逻辑
            insets
        }
// 手动触发重新分发 insets
        ViewCompat.requestApplyInsets(binding.root)
        loadSettings()
        setListeners()
    }


    /**
     * 加载已保存的设置
     */
    @SuppressLint("SetTextI18n")
    private fun loadSettings() {
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val cacheCount = prefs.getInt(KEY_CACHE_COUNT, 1000)
        binding.etCacheCount.setText(cacheCount.toString())
        YukiHookAPI.Status.isXposedModuleActive.let { isActive ->
            if (isActive) {
                binding.tvStatus.text = "模块已激活，当前缓存数量: $cacheCount\n重启抖音生效"
            } else {
                binding.tvStatus.text =
                    "模块未激活，当前缓存数量: $cacheCount\n请确保已启用模块并重启抖音"
            }
        }
    }

    /**
     * 设置按钮点击监听
     */
    private fun setListeners() {
        binding.btnSave.setOnClickListener { v: View? -> saveSettings() }
    }

    /**
     * 保存设置到 SharedPreferences
     * Xposed 模块会从同一个 SharedPreferences 文件读取这个值
     */
    @SuppressLint("SetTextI18n")
    private fun saveSettings() {
        val cacheCountStr = binding.etCacheCount.getText().toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(cacheCountStr)) {
            binding.tvStatus.text = "请输入缓存数量"
            return
        }

        try {
            val cacheCount = cacheCountStr.toInt()
            if (cacheCount <= 0) {
                binding.tvStatus.text = "缓存数量必须大于0"
                return
            }

            if (cacheCount > 10000) {
                binding.tvStatus.text = "缓存数量过大，建议不超过10000"
                return
            }
            // 保存到 SharedPreferences
            // 按照LSPosed New XSharedPreferences规范：使用MODE_WORLD_READABLE
            // LSPosed会自动hook ContextImpl.checkMode()使其生效
            var prefs: SharedPreferences
            try {
                prefs = getSharedPreferences(PREF_NAME, MODE_WORLD_READABLE)
                Log.d(TAG, "使用MODE_WORLD_READABLE成功")
            } catch (e: SecurityException) {
                // 如果LSPosed未启用或模块未加载，降级到MODE_PRIVATE
                Log.w(TAG, "MODE_WORLD_READABLE失败，降级到MODE_PRIVATE: " + e.message)
                prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            }

            val editor = prefs.edit()
            editor.putInt(KEY_CACHE_COUNT, cacheCount)
            val saved = editor.commit()

            Log.d(TAG, "commit结果: $saved")


            // 尝试手动设置文件权限
            // LSPosed New XSharedPreferences会将文件存储在随机目录
            // 我们需要找到实际文件并设置可读权限
            try {
                // 方法1：通过反射获取SharedPreferencesImpl的mFile字段
                val mFileField = prefs.javaClass.getDeclaredField("mFile")
                mFileField.setAccessible(true)
                val actualFile = mFileField.get(prefs) as File?

                Log.d(TAG, "实际SharedPreferences文件: " + actualFile!!.getAbsolutePath())
                Log.d(TAG, "文件存在: " + actualFile.exists())

                if (actualFile.exists()) {
                    // 设置文件权限为所有用户可读 (chmod 644)
                    val setReadable = actualFile.setReadable(true, false)
                    Log.d(TAG, "setReadable(true, false)结果: " + setReadable)
                    Log.d(TAG, "设置后可读: " + actualFile.canRead())

                    if (!setReadable) {
                        // 如果失败，尝试使用Runtime执行chmod命令
                        try {
                            val process = Runtime.getRuntime().exec(
                                arrayOf<String>(
                                    "chmod",
                                    "644",
                                    actualFile.getAbsolutePath()
                                )
                            )
                            val exitCode = process.waitFor()
                            Log.d(TAG, "chmod命令退出码: " + exitCode)
                        } catch (chmodEx: Exception) {
                            Log.w(TAG, "chmod命令执行失败: " + chmodEx.message)
                        }
                    }
                }
            } catch (reflectEx: Exception) {
                Log.w(TAG, "反射获取文件路径失败: " + reflectEx.message)


                // 方法2：遍历可能的路径
                val possiblePaths = arrayOf<String?>(
                    getApplicationInfo().dataDir + "/shared_prefs/" + PREF_NAME + ".xml",
                    "/data/misc/apexdata/" + getPackageName() + "/prefs/" + PREF_NAME + ".xml"
                )

                for (path in possiblePaths) {
                    val testFile = File(path)
                    if (testFile.exists()) {
                        Log.d(TAG, "找到文件: $path")
                        val setReadable = testFile.setReadable(true, false)
                        Log.d(TAG, "设置权限结果: " + setReadable + ", 可读: " + testFile.canRead())
                        break
                    }
                }
            }
            binding.tvStatus.text = "设置已保存 (缓存数量: $cacheCount)\n重启抖音生效"
            Log.d(TAG, "Saved cache count: $cacheCount")
        } catch (e: Exception) {
            binding.tvStatus.text = "保存设置失败: " + e.message
            Log.e(TAG, "Failed to save cache count", e)
        }
    }

    companion object {
        private const val TAG = "OfflineCacheSettings"
        private const val PREF_NAME = "OfflineCacheSettings"
        private const val KEY_CACHE_COUNT = "cache_count"
    }
}
