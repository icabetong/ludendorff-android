package io.capstone.ludendorff.features.request

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.Timestamp
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.AssetCore
import io.capstone.ludendorff.features.user.User
import io.capstone.ludendorff.features.user.UserCore
import kotlinx.parcelize.Parcelize

@Parcelize
data class Request @JvmOverloads constructor(
    var requestId: String = IDGenerator.generateRandom(),
    var asset: AssetCore? = null,
    var petitioner: UserCore? = null,
    var endorser: UserCore? = null,
    var endorsedTimestamp: Timestamp? = null,
    var submittedTimestamp: Timestamp? = null
): Parcelable {

    companion object {
        const val COLLECTION = "requests"
        const val FIELD_ID = "requestId"
        const val FIELD_ASSET = "asset"
        const val FIELD_ASSET_ID = "${FIELD_ASSET}.${Asset.FIELD_ID}"
        const val FIELD_ASSET_NAME = "${FIELD_ASSET}.${Asset.FIELD_NAME}"
        const val FIELD_PETITIONER  = "petitioner"
        const val FIELD_PETITIONER_ID = "${FIELD_PETITIONER}.${User.FIELD_ID}"
        const val FIELD_PETITIONER_NAME = "${FIELD_PETITIONER}.$${User.FIELD_NAME}"
        const val FIELD_ENDORSER = "endorser"
        const val FIELD_ENDORSER_ID = "${FIELD_ENDORSER}.${User.FIELD_ID}"
        const val FILED_ENDORSER_NAME = "${FIELD_ENDORSER}.${User.FIELD_NAME}"
        const val FIELD_ENDORSED_TIMESTAMP = "endorsedTimestamp"
        const val FIELD_SUBMITTED_TIMESTAMP = "submittedTimestamp"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Request>() {
            override fun areItemsTheSame(oldItem: Request, newItem: Request): Boolean {
                return oldItem.requestId == newItem.requestId
            }

            override fun areContentsTheSame(oldItem: Request, newItem: Request): Boolean {
                return oldItem == newItem
            }
        }
    }
}