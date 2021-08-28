package io.capstone.ludendorff.features.core.services

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.features.core.worker.TokenUpdateWorker

class NotificationServices: FirebaseMessagingService() {

    private val userPreferences = UserPreferences(this)
    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(applicationContext)
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)

        userPreferences.deviceToken = newToken
        val deviceTokenRequest = OneTimeWorkRequestBuilder<TokenUpdateWorker>()
            .addTag(TokenUpdateWorker.WORKER_TAG)
            .setInputData(workDataOf(TokenUpdateWorker.EXTRA_TOKEN_ID to newToken))
            .build()
        workManager.enqueueUniqueWork(TokenUpdateWorker.WORKER_TAG,
            ExistingWorkPolicy.REPLACE, deviceTokenRequest)
    }

}