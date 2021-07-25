package io.capstone.keeper.features.user

import android.os.Parcelable
import io.capstone.keeper.components.utils.IDGenerator
import kotlinx.android.parcel.Parcelize

/**
 *  This data class is used in querying
 *  the minimal information the user can be
 *  associated to.
 */
@Parcelize
data class UserCore @JvmOverloads constructor(
    var userId: String = IDGenerator.generateRandom(),
    var name: String? = null,
    var email: String? = null,
    var imageUrl: String? = null,
    var position: String? = null,
): Parcelable
