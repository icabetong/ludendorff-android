package io.capstone.ludendorff.api.request

import android.os.Parcelable
import io.capstone.ludendorff.features.user.User
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
class RemoveUserRequest (
    var token: String,
    var userId: String
): Parcelable {

    fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(TOKEN, token)
            put(USER_ID, userId)
        }
    }

    companion object {
        const val TOKEN = "token"
        const val USER_ID = User.FIELD_ID
    }
}