package io.capstone.ludendorff.features.assignment

import android.content.Context
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.helper.highlighting.Highlightable
import com.algolia.search.model.Attribute
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import io.capstone.ludendorff.components.extensions.format
import io.capstone.ludendorff.components.serialization.TimestampSerializer
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.AssetCore
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.user.User
import io.capstone.ludendorff.features.user.UserCore
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
@Parcelize
data class Assignment @JvmOverloads constructor(
    var assignmentId: String = IDGenerator.generateRandom(),
    var asset: AssetCore? = null,
    var user: UserCore? = null,
    @Serializable(TimestampSerializer::class)
    var dateAssigned: Timestamp? = null,
    @Serializable(TimestampSerializer::class)
    var dateReturned: Timestamp? = null,
    var location: String? = null,
    var remarks: String? = null,
    @Exclude
    override val _highlightResult: @RawValue JsonObject? = null
): Parcelable, Highlightable {

    val highlightedAssetName: HighlightedString?
        get() = getHighlight(Attribute(FIELD_ASSET_NAME))
    val highlightedCategoryName: HighlightedString?
        get() = getHighlight(Attribute(FIELD_CATEGORY_NAME))
    val highlightedUserName: HighlightedString?
        get() = getHighlight(Attribute(FIELD_USER_NAME))

    private fun formatTimestamp(timestamp: Timestamp?, context: Context): String? {
        return timestamp?.format(context)
    }

    fun formatDateAssigned(context: Context): String? {
        return formatTimestamp(dateAssigned, context)
    }

    fun formatDateReturned(context: Context): String? {
        return formatTimestamp(dateReturned, context)
    }

    companion object {
        const val COLLECTION = "assignments"
        const val FIELD_ID = "assignmentId"
        const val FIELD_ASSET = "asset"
        const val FIELD_ASSET_ID = "${FIELD_ASSET}.${Asset.FIELD_ID}"
        const val FIELD_ASSET_NAME = "${FIELD_ASSET}.${Asset.FIELD_NAME}"
        const val FIELD_CATEGORY = "${FIELD_ASSET}.${Asset.FIELD_CATEGORY}"
        const val FIELD_CATEGORY_ID = "${FIELD_ASSET}.${Asset.FIELD_CATEGORY}.${Category.FIELD_ID}"
        const val FIELD_CATEGORY_NAME = "${FIELD_ASSET}.${Asset.FIELD_CATEGORY}.${Category.FIELD_NAME}"
        const val FIELD_USER = "user"
        const val FIELD_USER_ID = "${FIELD_USER}.${User.FIELD_ID}"
        const val FIELD_USER_NAME = "${FIELD_USER}.${User.FIELD_NAME}"
        const val FIELD_DATE_ASSIGNED = "dateAssigned"
        const val FIELD_DATE_RETURNED = "dateReturned"
        const val FIELD_LOCATION = "location"
        const val FIELD_REMARKS = "remarks"

        fun from(documentSnapshot: DocumentSnapshot): Assignment? {
            return documentSnapshot.toObject(Assignment::class.java)
        }

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Assignment>() {
            override fun areContentsTheSame(oldItem: Assignment, newItem: Assignment): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Assignment, newItem: Assignment): Boolean {
                return oldItem.assignmentId == newItem.assignmentId
            }
        }
    }
}
