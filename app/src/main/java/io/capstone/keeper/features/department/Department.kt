package io.capstone.keeper.features.department

import android.os.Parcelable
import io.capstone.keeper.components.utils.IDGenerator
import io.capstone.keeper.features.user.UserCore
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Department @JvmOverloads constructor(
    var departmentId: String = IDGenerator.generateRandom(),
    var name: String? = null,
    var managerSSN: UserCore? = null
): Parcelable {

    companion object {
        const val COLLECTION = "departments"
        const val FIELD_ID = "departmentId"
        const val FIELD_NAME = "name"
        const val FIELD_MANAGER_SSN = "managerSSN"
        const val FIELD_TYPE = "type"
    }
}