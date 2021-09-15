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
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.features.core.activities.MainActivity
import io.capstone.ludendorff.features.core.worker.TokenUpdateWorker
import io.capstone.ludendorff.features.notification.Notification

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
        val titleLocKey = remoteMessage.data[Notification.FIELD_TITLE]
        val bodyLocKey = remoteMessage.data[Notification.FIELD_BODY]

        val titleRes = resources.getIdentifier(titleLocKey,"string", this.packageName)
        val bodyRes = resources.getIdentifier(bodyLocKey,"string", this.packageName)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EXTRA_NOTIFICATION_ID, 0)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val channel = if (titleLocKey == Notification.NOTIFICATION_ASSIGNED_TITLE)
            getString(R.string.channel_id_assignments)
        else getString(R.string.channel_id_requests)

        createChannels(this)
        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.ic_icon_pickelhaube)
            .setContentTitle(String.format(getString(titleRes),
                remoteMessage.data[Notification.EXTRA_TARGET]))
            .setContentText(String.format(getString(bodyRes),
                remoteMessage.data[Notification.EXTRA_SENDER],
                remoteMessage.data[Notification.EXTRA_TARGET]))
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

                val channels = mapOf(
                    getString(R.string.channel_id_assignments)
                            to getString(R.string.channel_name_assignments),
                    getString(R.string.channel_id_requests)
                            to getString(R.string.channel_name_requests)
                )
                channels.forEach { (id, name) ->
                    if (notificationManager?.getNotificationChannel(id) != null)
                        return@with

                    notificationManager?.createNotificationChannel(
                        NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
                    )
                }
            }
        }
    }

}