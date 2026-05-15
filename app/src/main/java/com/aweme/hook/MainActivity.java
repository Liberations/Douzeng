package com.aweme.hook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aweme.hook.R;

/**
 * 主界面 - 用于设置离线缓存数量
 * 该界面将设置保存到 SharedPreferences，Xposed 模块会读取这个值
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "OfflineCacheSettings";
    private static final String PREF_NAME = "OfflineCacheSettings";
    private static final String KEY_CACHE_COUNT = "cache_count";
    
    private EditText etCacheCount;
    private Button btnSave;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        loadSettings();
        setListeners();
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        etCacheCount = findViewById(R.id.et_cache_count);
        btnSave = findViewById(R.id.btn_save);
        tvStatus = findViewById(R.id.tv_status);
    }
    
    /**
     * 加载已保存的设置
     */
    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int cacheCount = prefs.getInt(KEY_CACHE_COUNT, 1000);
        etCacheCount.setText(String.valueOf(cacheCount));
    }
    
    /**
     * 设置按钮点击监听
     */
    private void setListeners() {
        btnSave.setOnClickListener(v -> saveSettings());
    }
    
    /**
     * 保存设置到 SharedPreferences
     * Xposed 模块会从同一个 SharedPreferences 文件读取这个值
     */
    private void saveSettings() {
        String cacheCountStr = etCacheCount.getText().toString().trim();
        if (TextUtils.isEmpty(cacheCountStr)) {
            tvStatus.setText("请输入缓存数量");
            return;
        }
        
        try {
            int cacheCount = Integer.parseInt(cacheCountStr);
            if (cacheCount <= 0) {
                tvStatus.setText("缓存数量必须大于0");
                return;
            }
            
            if (cacheCount > 10000) {
                tvStatus.setText("缓存数量过大，建议不超过10000");
                return;
            }
            // 保存到 SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_CACHE_COUNT, cacheCount);
            boolean saved = editor.commit();
            Log.d(TAG, "SharedPreferences commit结果: " + saved);
            tvStatus.setText("设置已保存 (缓存数量: " + cacheCount + ")\n重启抖音生效");
            Log.d(TAG, "Saved cache count: " + cacheCount);
        } catch (Exception e) {
            tvStatus.setText("保存设置失败: " + e.getMessage());
            Log.e(TAG, "Failed to save cache count", e);
        }
    }
}
