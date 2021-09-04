package io.capstone.ludendorff.features.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.capstone.ludendorff.R
import io.capstone.ludendorff.api.NotificationRequest
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.features.core.activities.MainActivity
import io.capstone.ludendorff.features.core.worker.TokenUpdateWorker

class NotificationServices: FirebaseMessagingService() {

    private val userPreferences = UserPreferences(this)
    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(applicationContext)
    }
    private val notificationManager: NotificationManager? by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EXTRA_NOTIFICATION_ID, 0)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        createChannels(this)
        val notification = NotificationCompat.Builder(this,
            getString(R.string.channel_id_assignments))
            .setSmallIcon(R.drawable.ic_icon_pickelhaube)
            .setContentTitle(getString(R.string.notification_assigned_asset_title))
            .setContentText(getString(R.string.notification_assigned_asset_body))
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)

        notificationManager?.notify(0, notification.build())
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra:notification_id"

        fun createChannels(context: Context) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O)
                return;

            with(context) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                    as? NotificationManager

                val channelId = getString(R.string.channel_id_assignments)
                if (notificationManager?.getNotificationChannel(channelId) != null)
                    return@with;

                val channel = NotificationChannel(channelId, getString(R.string.channel_name_assignments),
                    NotificationManager.IMPORTANCE_HIGH)

                notificationManager?.createNotificationChannel(channel)
            }
        }
    }

}