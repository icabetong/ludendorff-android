package io.capstone.ludendorff.features.department

import android.os.Parcelable
import io.capstone.ludendorff.components.utils.IDGenerator
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 *  This data class is used in querying
 *  the minimal information the department can be
 *  associated to.
 */
@Serializable
@Parcelize
data class DepartmentCore @JvmOverloads constructor(
    var departmentId: String = IDGenerator.generateRandom(),
    var name: String? = null
): Parcelable {

    companion object {
        fun from(department: Department): DepartmentCore {
            return DepartmentCore(
                departmentId = department.departmentId,
                name = department.name
            )
        }
    }
}
