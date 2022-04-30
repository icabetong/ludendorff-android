package io.capstone.ludendorff.features.issued.item

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntry
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Keep
@Parcelize
data class IssuedItem @JvmOverloads constructor(
    var stockNumber: String = "",
    var description: String? = null,
    var unitOfMeasure: String? = null,
    var quantityIssued: Int = 0,
    // same with unit Value in Asset entity
    var unitCost: Double = 0.0,
    var responsibilityCenter: String? = null,
): Parcelable {

    fun toJSONObject(): JSONObject {
        return JSONObject().also {
            it.put(FIELD_STOCK_NUMBER, stockNumber)
            it.put(FIELD_DESCRIPTION, description)
            it.put(FIELD_UNIT_OF_MEASURE, unitOfMeasure)
            it.put(FIELD_QUANTITY_ISSUED, quantityIssued)
            it.put(FIELD_UNIT_COST, unitCost)
            it.put(FIELD_RESPONSIBILITY_CENTER, responsibilityCenter)
        }
    }

    fun toStockCardEntry(ref: String?): StockCardEntry {
        return StockCardEntry(reference = ref, issueQuantity = this.quantityIssued,
            issueOffice = this.responsibilityCenter)
    }

    companion object {
        const val FIELD_STOCK_NUMBER = "stockNumber"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_UNIT_OF_MEASURE = "unitOfMeasure"
        const val FIELD_QUANTITY_ISSUED = "quantityIssued"
        const val FIELD_UNIT_COST = "unitCost"
        const val FIELD_RESPONSIBILITY_CENTER = "responsibilityCenter"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<IssuedItem>() {
            override fun areItemsTheSame(oldItem: IssuedItem, newItem: IssuedItem): Boolean {
                return oldItem.stockNumber == newItem.stockNumber
            }

            override fun areContentsTheSame(oldItem: IssuedItem, newItem: IssuedItem): Boolean {
                return oldItem == newItem
            }
        }

        fun fromAsset(asset: Asset): IssuedItem {
            return IssuedItem(
                stockNumber = asset.stockNumber,
                description = asset.description,
                unitOfMeasure = asset.unitOfMeasure,
                unitCost = asset.unitValue
            )
        }
    }
}