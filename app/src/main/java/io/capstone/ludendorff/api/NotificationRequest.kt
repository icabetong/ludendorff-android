package io.capstone.ludendorff.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

@Parcelize
data class NotificationRequest (
    var token: String,
    var deviceToken: String,
    var notificationTitle: String?,
    var notificationBody: String?,
    var data: Map<String, String>
): Parcelable {

    fun toRequestBody(): RequestBody {
        val type = MediaType.get("application/json; charset=utf-8")

        return RequestBody.create(type, this.toJSONObject().toString())
    }

    private fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(FIELD_TOKEN, token)
            put(FIELD_DEVICE, deviceToken)
            put(FIELD_NOTIFICATION, JSONObject().apply {
                put(FIELD_NOTIFICATION_TITLE, notificationTitle)
                put(FIELD_NOTIFICATION_BODY, notificationBody)
            })
            put(FIELD_DATA, JSONObject(data))
        }
    }

    companion object {
        const val FIELD_TOKEN = "token"
        const val FIELD_DEVICE = "deviceToken"
        const val FIELD_NOTIFICATION = "notification"
        const val FIELD_NOTIFICATION_TITLE = "title"
        const val FIELD_NOTIFICATION_BODY = "body"
        const val FIELD_DATA = "data"
    }
}