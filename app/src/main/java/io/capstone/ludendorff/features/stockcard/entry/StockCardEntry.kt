package io.capstone.ludendorff.features.stockcard.entry

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.Timestamp
import io.capstone.ludendorff.components.utils.IDGenerator
import kotlinx.parcelize.Parcelize

@Parcelize
data class StockCardEntry @JvmOverloads constructor(
    var stockCardEntryId: String = IDGenerator.generateRandom(),
    var date: Timestamp = Timestamp.now(),
    var reference: String? = null,
    var receiptQuantity: Int = 0,
    var requestedQuantity: Int = 0,
    var issueQuantity: Int = 0,
    var issueOffice: String? = null,
    var balanceQuantity: Int = 0,
    var balanceTotalPrice: Double = 0.0
): Parcelable {

    companion object {
        const val FIELD_STOCK_ENTRY_ID = "stockCardEntryId"
        const val FIELD_DATE = "date"
        const val FIELD_REFERENCE = "reference"
        const val FIELD_RECEIPT_QUANTITY = "receiptQuantity"
        const val FIELD_REQUEST_QUANTITY = "requestQuantity"
        const val FIELD_ISSUE_QUANTITY = "issueQuantity"
        const val FIELD_ISSUE_OFFICE = "issueOffice"
        const val FIELD_BALANCE_QUANTITY = "balanceQuantity"
        const val FIELD_BALANCE_TOTAL_PRICE = "balanceTotalPrice"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<StockCardEntry>() {
            override fun areItemsTheSame(
                oldItem: StockCardEntry,
                newItem: StockCardEntry
            ): Boolean {
                return oldItem.stockCardEntryId == newItem.stockCardEntryId
            }

            override fun areContentsTheSame(
                oldItem: StockCardEntry,
                newItem: StockCardEntry
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}