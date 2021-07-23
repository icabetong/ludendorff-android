package io.capstone.keeper.features.user

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import io.capstone.keeper.components.utils.IDGenerator
import io.capstone.keeper.features.department.Department
import io.capstone.keeper.features.department.DepartmentCore
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User @JvmOverloads constructor(
    var userId: String = IDGenerator.generateRandom(),
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var imageUrl: String? = null,
    var permissions: Int = 0,
    var position: String? = null,
    var department: DepartmentCore? = null
): Parcelable {

    fun getDisplayName(): String {
        return "$firstName $lastName"
    }

    companion object {
        const val COLLECTION = "users"
        const val FIELD_ID = "userId"
        const val FIELD_FIRST_NAME = "firstName"
        const val FIELD_LAST_NAME = "lastName"
        const val FIELD_EMAIL = "email"
        const val FIELD_IMAGE_URL = "imageUrl"
        const val FIELD_PERMISSIONS = "permissions"
        const val FIELD_POSITION = "position"
        const val FIELD_DEPARTMENT = "department"
        const val FIELD_DEPARTMENT_ID = "${FIELD_DEPARTMENT}.${Department.FIELD_ID}"
        const val FIELD_DEPARTMENT_NAME = "${FIELD_DEPARTMENT}.${Department.FIELD_NAME}"

        const val PERMISSION_READ = 1
        const val PERMISSION_WRITE = 2
        const val PERMISSION_DELETE = 4
        const val PERMISSION_AUDIT = 8
        const val PERMISSION_MANAGE_USERS = 16
        const val PERMISSION_ADMINISTRATIVE = 32

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}