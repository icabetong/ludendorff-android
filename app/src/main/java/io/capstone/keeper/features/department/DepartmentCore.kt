package io.capstone.keeper.features.department

import android.os.Parcelable
import io.capstone.keeper.components.utils.IDGenerator
import kotlinx.android.parcel.Parcelize

/**
 *  This data class is used in querying
 *  the minimal information the department can be
 *  associated to.
 */
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
