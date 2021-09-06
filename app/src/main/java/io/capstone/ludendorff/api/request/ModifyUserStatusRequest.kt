package io.capstone.ludendorff.api.request

import android.os.Parcelable
import io.capstone.ludendorff.features.user.User
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
class ModifyUserStatusRequest (
    var token: String,
    var userId: String,
    var disabled: Boolean
): Parcelable {

    fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(TOKEN, token)
            put(USER_ID, userId)
            put(DISABLED, disabled)
        }
    }

    companion object {
        const val TOKEN = "token"
        const val USER_ID = User.FIELD_ID
        const val DISABLED = User.FIELD_DISABLED
    }
}