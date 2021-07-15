package io.capstone.keeper.features.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 *  This data class is used in querying
 *  the minimal information the user can be
 *  associated to.
 */
@Parcelize
data class UserCore @JvmOverloads constructor(
    var userId: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var position: String? = null,
    var department: String? = null
): Parcelable
