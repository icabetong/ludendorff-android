package io.capstone.ludendorff.features.notification

import android.content.Context
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.Timestamp
import io.capstone.ludendorff.components.extensions.format
import kotlinx.parcelize.Parcelize

@Parcelize
data class Notification @JvmOverloads constructor (
    var notificationId: String,
    var title: String?,
    var body: String?,
    var payload: String?,
    var senderId: String?,
    var receiverId: String?,
    var timestamp: Timestamp? = null
): Parcelable {

    fun formatTimestamp(context: Context): String? {
        return timestamp?.format(context)
    }

    companion object {
        const val COLLECTION = "notifications"
        const val FIELD_ID = "notificationId"
        const val FIELD_TITLE = "title"
        const val FIELD_BODY = "body"
        const val FIELD_PAYLOAD = "payload"
        const val FIELD_SENDER_ID = "senderId"
        const val FIELD_RECEIVER_ID = "receiverId"

        const val NOTIFICATION_ASSIGNED_TITLE = "notification-assigned-asset-title"
        const val NOTIFICATION_ASSIGNED_BODY = "notification-assigned-asset-body"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Notification>() {
            override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem.notificationId == newItem.notificationId
            }
        }
    }

}