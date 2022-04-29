package io.capstone.ludendorff.features.stockcard

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.firestore.Exclude
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.shared.data.BalanceEntry
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntry
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class StockCard @JvmOverloads constructor(
    var stockCardId: String = IDGenerator.generateRandom(),
    var entityName: String? = null,
    var stockNumber: String = "",
    var description: String? = null,
    var unitPrice: Double = 0.0,
    var unitOfMeasure: String? = null,
    var balances: Map<String, BalanceEntry> = emptyMap(),
    @get:Exclude
    var entries: List<StockCardEntry> = emptyList()
): Parcelable {

    companion object {
        const val COLLECTION = "cards"
        const val FIELD_STOCK_CARD_ID = "stockCardId"
        const val FIELD_ENTITY_NAME = "entityName"
        const val FIELD_STOCK_NUMBER = "stockNumber"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_UNIT_PRICE = "unitPrice"
        const val FIELD_UNIT_OF_MEASURE = "unitOfMeasure"
        const val FIELD_BALANCES = "balances"
        const val FIELD_ENTRIES = "entries"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<StockCard>() {
            override fun areItemsTheSame(oldItem: StockCard, newItem: StockCard): Boolean {
                return oldItem.stockCardId == newItem.stockCardId
            }

            override fun areContentsTheSame(oldItem: StockCard, newItem: StockCard): Boolean {
                return oldItem == newItem
            }

        }
    }
}

