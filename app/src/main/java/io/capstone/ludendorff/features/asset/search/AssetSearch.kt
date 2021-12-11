package io.capstone.ludendorff.features.asset.search

import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.helper.highlighting.Highlightable
import com.algolia.search.model.Attribute
import com.google.firebase.Timestamp
import io.capstone.ludendorff.components.serialization.TimestampSerializer
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.category.CategoryCore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Keep
@Serializable
data class AssetSearch @JvmOverloads constructor(
    var assetId: String = IDGenerator.generateRandom(),
    var assetName: String? = null,
    @Serializable(TimestampSerializer::class)
    var dateCreated: Timestamp? = null,
    var status: Asset.Status? = null,
    var category: CategoryCore? = null,
    var specifications: Map<String, String> = emptyMap(),
    override val _highlightResult: JsonObject? = null
): Highlightable {

    val highlightedName: HighlightedString?
        get() = getHighlight(Attribute(Asset.FIELD_NAME))
    val highlightedCategory: HighlightedString?
        get() = getHighlight(Attribute(Category.FIELD_NAME))

    fun toAsset(): Asset {
        return Asset(
            assetId = this.assetId,
            assetName = this.assetName,
            dateCreated = this.dateCreated,
            status = this.status,
            category = this.category,
            specifications = this.specifications
        )
    }

    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<AssetSearch>() {
            override fun areContentsTheSame(oldItem: AssetSearch, newItem: AssetSearch): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: AssetSearch, newItem: AssetSearch): Boolean {
                return oldItem.assetId == newItem.assetId
            }
        }
    }
}