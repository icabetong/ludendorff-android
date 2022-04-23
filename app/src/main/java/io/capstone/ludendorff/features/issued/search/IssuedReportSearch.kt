package io.capstone.ludendorff.features.issued.search

import androidx.recyclerview.widget.DiffUtil
import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.helper.highlighting.Highlightable
import com.algolia.search.model.Attribute
import com.google.firebase.Timestamp
import io.capstone.ludendorff.components.serialization.TimestampSerializer
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.issued.IssuedReport
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 *  Report on Supplies and Materials Issued
 */

@Serializable
data class IssuedReportSearch @JvmOverloads constructor(
    var issuedReportId: String = IDGenerator.generateRandom(),
    var entityName: String? = null,
    var fundCluster: String? = null,
    var serialNumber: String? = null,
    @Serializable(TimestampSerializer::class)
    var date: Timestamp = Timestamp.now(),
    override val _highlightResult: JsonObject?
): Highlightable {

    fun toIssuedReport(): IssuedReport {
        return IssuedReport(
            issuedReportId = this.issuedReportId,
            fundCluster = this.fundCluster,
            serialNumber = this.serialNumber,
            date = this.date,
        )
    }

    val highlightedFundCluster: HighlightedString?
        get() = getHighlight(Attribute(IssuedReport.FIELD_FUND_CLUSTER))
    val highlightedSerialNumber: HighlightedString?
        get() = getHighlight(Attribute(IssuedReport.FIELD_SERIAL_NUMBER))
    val highlightedEntityName: HighlightedString?
        get() = getHighlight(Attribute(IssuedReport.FIELD_ENTITY_NAME))

    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<IssuedReportSearch>() {
            override fun areItemsTheSame(oldItem: IssuedReportSearch, newItem: IssuedReportSearch): Boolean {
                return oldItem.issuedReportId == newItem.issuedReportId
            }

            override fun areContentsTheSame(oldItem: IssuedReportSearch, newItem: IssuedReportSearch): Boolean {
                return oldItem == newItem
            }
        }
    }
}