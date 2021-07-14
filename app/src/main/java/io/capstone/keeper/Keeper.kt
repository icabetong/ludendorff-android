package io.capstone.keeper

import android.app.Application
import android.content.pm.ApplicationInfo
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Keeper: Application() {

    override fun onCreate() {
        super.onCreate()

        val isDebugBuild = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        FirebaseAnalytics.getInstance(this)
            .setAnalyticsCollectionEnabled(isDebugBuild)
    }
}