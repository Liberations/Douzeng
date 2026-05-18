package com.aweme.hook

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MyApp: Application(){
    override fun onCreate() {
        super.onCreate()
        /** 跟随系统夜间模式 */
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}