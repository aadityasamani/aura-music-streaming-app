package com.antigravity.aura

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AuraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.antigravity.aura.youtube.YouTubeStreamFetcher.init()
    }
}
