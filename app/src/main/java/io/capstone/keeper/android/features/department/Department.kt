package io.capstone.keeper.android.features.department

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Department @JvmOverloads constructor(
    var departmentId: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var managerSSN: String? = null,
    var type: String? = null
): Parcelable
