package io.capstone.ludendorff.api.request

import android.os.Parcelable
import io.capstone.ludendorff.features.department.Department
import io.capstone.ludendorff.features.user.User
import kotlinx.parcelize.Parcelize
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

@Parcelize
data class CreateUserRequest (
    var token: String,
    var user: User?
): Parcelable {

    fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(FIELD_TOKEN, token)
            put(FIELD_USER_ID, user?.userId)
            put(FIELD_EMAIL, user?.email)
            put(FIELD_FIRST_NAME, user?.firstName)
            put(FIELD_LAST_NAME, user?.lastName)
            put(FIELD_POSITION, user?.position)
            put(FIELD_PERMISSIONS, user?.permissions)
            put(FIELD_DEPARTMENT, JSONObject().apply {
                put(FIELD_DEPARTMENT_ID, user?.department?.departmentId)
                put(FIELD_DEPARTMENT_NAME, user?.department?.name)
            })
        }
    }

    companion object {
        const val FIELD_TOKEN = "token"
        const val FIELD_USER_ID = User.FIELD_ID
        const val FIELD_EMAIL = User.FIELD_EMAIL
        const val FIELD_FIRST_NAME = User.FIELD_FIRST_NAME
        const val FIELD_LAST_NAME = User.FIELD_LAST_NAME
        const val FIELD_POSITION = User.FIELD_POSITION
        const val FIELD_PERMISSIONS = User.FIELD_PERMISSIONS
        const val FIELD_DEPARTMENT = User.FIELD_DEPARTMENT
        const val FIELD_DEPARTMENT_ID = Department.FIELD_ID
        const val FIELD_DEPARTMENT_NAME = Department.FIELD_NAME
    }
}