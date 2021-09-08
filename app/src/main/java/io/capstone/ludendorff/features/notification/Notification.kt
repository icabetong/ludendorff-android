package io.capstone.ludendorff.features.notification

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Notification @JvmOverloads constructor (
    var notificationId: String? = null,
    var title: String?,
    var body: String?,
    var payload: String?,
    var senderId: String?,
    var receiverId: String?
): Parcelable {

    companion object {
        const val COLLECTION = "notifications"
        const val FIELD_ID = "notificationId"
        const val FIELD_TITLE = "title"
        const val FIELD_BODY = "body"
        const val FIELD_PAYLOAD = "payload"
        const val FIELD_SENDER_ID = "senderId"
        const val FIELD_RECEIVER_ID = "receiverId"
    }

}