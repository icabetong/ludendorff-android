package io.capstone.keeper.features.department

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 *  This data class is used in querying
 *  the minimal information the department can be
 *  associated to.
 */
@Parcelize
data class DepartmentCore @JvmOverloads constructor(
    var departmentId: String = UUID.randomUUID().toString(),
    var name: String? = null
): Parcelable
