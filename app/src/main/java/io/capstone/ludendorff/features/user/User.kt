package io.capstone.ludendorff.features.user

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.helper.highlighting.Highlightable
import com.algolia.search.model.Attribute
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import io.capstone.ludendorff.components.interfaces.Serializable
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.department.Department
import io.capstone.ludendorff.features.department.DepartmentCore
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.json.JsonObject
import org.json.JSONArray
import org.json.JSONObject

@Keep
@kotlinx.serialization.Serializable
@Parcelize
data class User @JvmOverloads constructor(
    var userId: String = IDGenerator.generateRandom(),
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var imageUrl: String? = null,
    var permissions: List<Int> = emptyList(),
    var position: String? = null,
    var department: DepartmentCore? = null,
    var deviceToken: String? = null,
    var disabled: Boolean = false,
    @Exclude
    override val _highlightResult: @RawValue JsonObject? = null
): Parcelable, Serializable, Highlightable {

    val highlightedFirstName: HighlightedString?
        get() = getHighlight(Attribute(FIELD_FIRST_NAME))
    val highlightedLastName: HighlightedString?
        get() = getHighlight(Attribute(FIELD_LAST_NAME))
    val highlightedPosition: HighlightedString?
        get() = getHighlight((Attribute(FIELD_POSITION)))
    val highlightedEmail: HighlightedString?
        get() = getHighlight((Attribute(FIELD_EMAIL)))

    override fun toJSON(): JSONObject {
        return JSONObject().apply {
            put(FIELD_ID, userId)
            put(FIELD_EMAIL, email)
            put(FIELD_FIRST_NAME, firstName)
            put(FIELD_LAST_NAME, lastName)
            put(FIELD_POSITION, position)
            put(FIELD_PERMISSIONS, JSONArray(permissions.toIntArray()))
            put(FIELD_DEPARTMENT, JSONObject().apply {
                put(FIELD_DEPARTMENT_ID, department?.departmentId)
                put(FIELD_DEPARTMENT_NAME, department?.name)
            })
        }
    }

    fun hasPermission(permission: Int): Boolean {
        return permissions.contains(permission)
    }

    @Exclude
    fun getDisplayName(): String {
        return "$firstName $lastName"
    }

    fun minimize(): UserCore {
        return UserCore.from(this)
    }

    companion object {
        const val COLLECTION = "users"
        const val FIELD_ID = "userId"
        const val FIELD_NAME = "name"
        const val FIELD_FIRST_NAME = "firstName"
        const val FIELD_LAST_NAME = "lastName"
        const val FIELD_EMAIL = "email"
        const val FIELD_IMAGE_URL = "imageUrl"
        const val FIELD_PERMISSIONS = "permissions"
        const val FIELD_POSITION = "position"
        const val FIELD_DEPARTMENT = "department"
        const val FIELD_DEPARTMENT_ID = "${FIELD_DEPARTMENT}.${Department.FIELD_ID}"
        const val FIELD_DEPARTMENT_NAME = "${FIELD_DEPARTMENT}.${Department.FIELD_NAME}"
        const val FIELD_DEVICE_TOKEN = "deviceToken"
        const val FIELD_DISABLED = "disabled"

        const val PERMISSION_READ = 1
        const val PERMISSION_WRITE = 2
        const val PERMISSION_DELETE = 4
        const val PERMISSION_MANAGE_USERS = 8
        const val PERMISSION_ADMINISTRATIVE = 16

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }

        fun from(documentSnapshot: DocumentSnapshot): User? {
            return documentSnapshot.toObject(User::class.java)
        }
    }
}