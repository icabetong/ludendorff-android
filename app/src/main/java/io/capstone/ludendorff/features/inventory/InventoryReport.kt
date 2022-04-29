package io.capstone.ludendorff.features.inventory

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.inventory.item.InventoryItem
import kotlinx.parcelize.Parcelize

/**
 *  Report on Physical Count of Inventories
 */
@Keep
@Parcelize
data class InventoryReport @JvmOverloads constructor(
    var inventoryReportId: String = IDGenerator.generateRandom(),
    var fundCluster: String? = null,
    var yearMonth: String? = null,
    var accountabilityDate: Timestamp = Timestamp.now(),
    var entityName: String? = null,
    var entityPosition: String? = null,
    @get:Exclude
    var items: List<InventoryItem> = emptyList(),
): Parcelable {

    companion object {
        const val COLLECTION = "inventories"
        const val FIELD_REPORT_ID = "inventoryReportId"
        const val FIELD_FUND_CLUSTER = "fundCluster"
        const val FIELD_ENTITY_NAME = "entityName"
        const val FIELD_ENTITY_POSITION = "entityPosition"
        const val FIELD_YEAR_MONTH = "yearMonth"
        const val FIELD_ACCOUNTABILITY_DATE = "accountabilityDate"
        const val FIELD_ITEMS = "inventoryItems"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<InventoryReport>() {
            override fun areItemsTheSame(
                oldItem: InventoryReport,
                newItem: InventoryReport
            ): Boolean {
                return oldItem.inventoryReportId == newItem.inventoryReportId
            }

            override fun areContentsTheSame(
                oldItem: InventoryReport,
                newItem: InventoryReport
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}
