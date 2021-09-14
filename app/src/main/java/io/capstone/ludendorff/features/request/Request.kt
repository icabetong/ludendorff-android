package io.capstone.ludendorff.features.request

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.Timestamp
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.asset.AssetCore
import io.capstone.ludendorff.features.user.UserCore
import kotlinx.parcelize.Parcelize

@Parcelize
data class Request @JvmOverloads constructor(
    var requestId: String = IDGenerator.generateRandom(),
    var requestedAsset: AssetCore? = null,
    var petitioner: UserCore? = null,
    var endorser: UserCore? = null,
    var endorsedDate: Timestamp? = null,
): Parcelable {

    companion object {
        const val COLLECTION = "requests"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Request>() {
            override fun areItemsTheSame(oldItem: Request, newItem: Request): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Request, newItem: Request): Boolean {
                return oldItem.requestId == newItem.requestId
            }

        }
    }
}
