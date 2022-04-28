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
    var receivedQuantity: Int = 0,
    var requestedQuantity: Int = 0,
    var issueQuantity: Int = 0,
    var issueOffice: String? = null,
): Parcelable {

    companion object {
        const val FIELD_STOCK_ENTRY_ID = "stockCardEntryId"
        const val FIELD_DATE = "date"
        const val FIELD_REFERENCE = "reference"
        const val FIELD_RECEIVED_QUANTITY = "receivedQuantity"
        const val FIELD_REQUEST_QUANTITY = "requestQuantity"
        const val FIELD_ISSUE_QUANTITY = "issueQuantity"
        const val FIELD_ISSUE_OFFICE = "issueOffice"

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