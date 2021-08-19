package io.capstone.ludendorff.features.department

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.user.UserCore
import kotlinx.parcelize.Parcelize

@Parcelize
data class Department @JvmOverloads constructor(
    var departmentId: String = IDGenerator.generateRandom(),
    var name: String? = null,
    var managerSSN: UserCore? = null,
    var count: Int = 0
): Parcelable {

    fun minimize(): DepartmentCore {
        return DepartmentCore.from(this)
    }

    companion object {
        const val COLLECTION = "departments"
        const val FIELD_ID = "departmentId"
        const val FIELD_NAME = "name"
        const val FIELD_MANAGER_SSN = "managerSSN"
        const val FIELD_COUNT = "count"

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Department>() {
            override fun areItemsTheSame(oldItem: Department, newItem: Department): Boolean {
                return oldItem.departmentId == newItem.departmentId
            }

            override fun areContentsTheSame(oldItem: Department, newItem: Department): Boolean {
                return oldItem == newItem
            }
        }
    }
}