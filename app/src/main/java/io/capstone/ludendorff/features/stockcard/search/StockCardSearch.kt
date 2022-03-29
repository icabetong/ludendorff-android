package io.capstone.ludendorff.features.stockcard.search

import androidx.recyclerview.widget.DiffUtil
import com.algolia.instantsearch.helper.highlighting.Highlightable
import com.algolia.search.model.Attribute
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.stockcard.StockCard
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class StockCardSearch @JvmOverloads constructor(
    var stockCardId: String = IDGenerator.generateRandom(),
    var entityName: String? = null,
    var stockNumber: String = "",
    var description: String? = null,
    var unitPrice: Double = 0.0,
    var unitOfMeasure: String? = null,
    override val _highlightResult: JsonObject?
): Highlightable {

    val highlightableEntityName
        get() = getHighlight(Attribute(StockCard.FIELD_ENTITY_NAME))
    val highlightableDescription
        get() = getHighlight(Attribute(StockCard.FIELD_DESCRIPTION))
    val highlightableStockNumber
        get() = getHighlight(Attribute(StockCard.FIELD_STOCK_NUMBER))

    fun toStockCard(): StockCard {
        return StockCard(
            stockCardId = this.stockCardId,
            entityName = this.entityName,
            stockNumber = this.stockNumber,
            description = this.description,
            unitPrice = this.unitPrice,
            unitOfMeasure = this.unitOfMeasure
        )
    }

    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<StockCardSearch>() {
            override fun areItemsTheSame(oldItem: StockCardSearch, newItem: StockCardSearch): Boolean {
                return oldItem.stockCardId == newItem.stockCardId
            }

            override fun areContentsTheSame(oldItem: StockCardSearch, newItem: StockCardSearch): Boolean {
                return oldItem == newItem
            }
        }
    }
}

