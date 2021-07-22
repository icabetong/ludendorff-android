package io.capstone.keeper

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.ConfigurationInfo
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Keeper: Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        val isDebugBuild = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        FirebaseAnalytics.getInstance(this)
            .setAnalyticsCollectionEnabled(isDebugBuild)
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
}