package io.capstone.ludendorff.features.inventory.search

import androidx.recyclerview.widget.DiffUtil
import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.helper.highlighting.Highlightable
import com.algolia.search.model.Attribute
import com.google.firebase.Timestamp
import io.capstone.ludendorff.components.serialization.TimestampSerializer
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.inventory.InventoryReport
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 *  Report on Physical Count of Inventories
 */
@Serializable
data class InventoryReportSearch @JvmOverloads constructor(
    var inventoryReportId: String = IDGenerator.generateRandom(),
    var fundCluster: String? = null,
    var entityName: String? = null,
    var entityPosition: String? = null,
    var yearMonth: String? = null,
    @Serializable(TimestampSerializer::class)
    var accountabilityDate: Timestamp = Timestamp.now(),
    override val _highlightResult: JsonObject?
): Highlightable {

    fun toInventoryReport(): InventoryReport {
        return InventoryReport(
            inventoryReportId = this.inventoryReportId,
            fundCluster = this.fundCluster,
            entityName = this.entityName,
            entityPosition = this.entityPosition,
            yearMonth = this.yearMonth,
            accountabilityDate = this.accountabilityDate,
        )
    }

    val highlightedFundCluster: HighlightedString?
        get() = getHighlight(Attribute(InventoryReport.FIELD_FUND_CLUSTER))
    val highlightedEntityName: HighlightedString?
        get() = getHighlight(Attribute(InventoryReport.FIELD_ENTITY_NAME))
    val highlightedYearMonth: HighlightedString?
        get() = getHighlight(Attribute(InventoryReport.FIELD_YEAR_MONTH))

    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<InventoryReportSearch>() {
            override fun areItemsTheSame(
                oldItem: InventoryReportSearch,
                newItem: InventoryReportSearch
            ): Boolean {
                return oldItem.inventoryReportId == newItem.inventoryReportId
            }

            override fun areContentsTheSame(
                oldItem: InventoryReportSearch,
                newItem: InventoryReportSearch
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}
