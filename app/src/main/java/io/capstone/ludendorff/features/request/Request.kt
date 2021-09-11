package io.capstone.ludendorff.features.request

import android.os.Parcelable
import com.google.firebase.Timestamp
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.asset.AssetCore
import io.capstone.ludendorff.features.user.UserCore
import kotlinx.parcelize.Parcelize

@Parcelize
data class Request @JvmOverloads constructor(
    var requestId: String = IDGenerator.generateRandom(),
    var requestedAsset: AssetCore? = null,
    var pertitioner: UserCore? = null,
    var endorser: UserCore? = null,
    var endorsedDate: Timestamp? = null,
): Parcelable {

    companion object {
        const val COLLECTION = "requests"
    }
}
