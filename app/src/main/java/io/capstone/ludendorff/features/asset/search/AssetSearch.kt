package io.capstone.ludendorff.features.asset.search

import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.helper.highlighting.Highlightable
import com.algolia.search.model.Attribute
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.category.CategoryCore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Keep
@Serializable
data class AssetSearch @JvmOverloads constructor(
    var stockNumber: String = "",
    var description: String? = null,
    var classification: String? = null,
    var category: CategoryCore? = null,
    var unitOfMeasure: String? = null,
    var unitValue: Double = 0.0,
    var remarks: String? = null,
    override val _highlightResult: JsonObject? = null
): Highlightable {

    val highlightedName: HighlightedString?
        get() = getHighlight(Attribute(Asset.FIELD_DESCRIPTION))
    val highlightedCategory: HighlightedString?
        get() = getHighlight(Attribute(Asset.FIELD_SUBCATEGORY))

    fun toAsset(): Asset {
        return Asset(
            stockNumber = this.stockNumber,
            description = this.description,
            subcategory = this.classification,
            category = this.category,
            unitOfMeasure = this.unitOfMeasure,
            unitValue = this.unitValue,
            remarks = this.remarks,
        )
    }

    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<AssetSearch>() {
            override fun areContentsTheSame(oldItem: AssetSearch, newItem: AssetSearch): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: AssetSearch, newItem: AssetSearch): Boolean {
                return oldItem.stockNumber == newItem.stockNumber
            }
        }
    }
}