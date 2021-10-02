package io.capstone.ludendorff.features.user

import android.os.Parcelable
import io.capstone.ludendorff.components.utils.IDGenerator
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 *  This data class is used in querying
 *  the minimal information the user can be
 *  associated to.
 */
@Serializable
@Parcelize
data class UserCore @JvmOverloads constructor(
    var userId: String = IDGenerator.generateRandom(),
    var name: String? = null,
    var email: String? = null,
    var imageUrl: String? = null,
    var position: String? = null,
    var deviceToken: String? = null
): Parcelable {

    companion object {
        fun from(user: User): UserCore {
            return UserCore(
                userId = user.userId,
                name = user.getDisplayName(),
                email = user.email,
                imageUrl = user.imageUrl,
                position = user.position,
                deviceToken = user.deviceToken
            )
        }
    }
}
