package io.capstone.keeper.features.user

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import io.capstone.keeper.features.department.DepartmentCore
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class User @JvmOverloads constructor(
    var userId: String = UUID.randomUUID().toString(),
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var permissions: Int = 0,
    var position: String? = null,
    var department: DepartmentCore? = null
): Parcelable {

    fun getDisplayName(): String {
        return "$firstName $lastName"
    }

    companion object {
        const val COLLECTION = "users"

        const val PERMISSION_READ = 1
        const val PERMISSION_CREATE = 2
        const val PERMISSION_UPDATE = 4
        const val PERMISSION_DELETE = 8
        const val PERMISSION_AUDIT = 16
        const val PERMISSION_MANAGE_USERS = 32
        const val PERMISSION_ADMINISTRATIVE = 64

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