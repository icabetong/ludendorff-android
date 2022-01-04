package io.capstone.ludendorff.features.request.search

import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.algolia.instantsearch.helper.highlighting.Highlightable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import io.capstone.ludendorff.components.serialization.TimestampSerializer
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.asset.AssetCore
import io.capstone.ludendorff.features.request.Request
import io.capstone.ludendorff.features.user.UserCore
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Keep
@Serializable
data class RequestSearch @JvmOverloads constructor(
    var requestId: String = IDGenerator.generateRandom(),
    var asset: AssetCore? = null,
    var petitioner: UserCore? = null,
    var endorser: UserCore? = null,
    @Serializable(with = TimestampSerializer::class)
    var endorsedTimestamp: Timestamp? = null,
    @Serializable(with = TimestampSerializer::class)
    var submittedTimestamp: Timestamp? = null,
    @Exclude
    override var _highlightResult: @RawValue JsonObject? = null
): Highlightable {

    fun toRequest(): Request {
        return Request(
            requestId = this.requestId,
            asset = this.asset,
            petitioner = this.petitioner,
            endorser = this.endorser,
            endorsedTimestamp = this.endorsedTimestamp,
            submittedTimestamp = this.submittedTimestamp
        )
    }

    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<RequestSearch>() {
            override fun areItemsTheSame(oldItem: RequestSearch, newItem: RequestSearch): Boolean {
                return oldItem.requestId == newItem.requestId
            }

            override fun areContentsTheSame(oldItem: RequestSearch, newItem: RequestSearch): Boolean {
                return oldItem == newItem
            }
        }
    }
}