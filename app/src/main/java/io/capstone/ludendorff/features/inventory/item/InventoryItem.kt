package io.capstone.ludendorff.features.inventory.item

import android.os.Parcelable
import androidx.annotation.Keep
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.category.CategoryCore
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Keep
@Parcelize
class InventoryItem @JvmOverloads constructor(
    var stockNumber: String = "",
    var article: String? = null,
    var description: String? = null,
    var category: CategoryCore? = null,
    var unitOfMeasure: String? = null,
    var unitValue: Double = 0.0,
    var balancePerCard: Int = 0,
    var onHandCount: Int = 0,
    var remarks: String? = null,
    var supplier: String? = null,
): Parcelable {

    fun toJSONObject(): JSONObject {
        return JSONObject().also {
            it.put(FIELD_STOCK_NUMBER, stockNumber)
            it.put(FIELD_ARTICLE, article)
            it.put(FIELD_DESCRIPTION, description)
            it.put(FIELD_TYPE, category)
            it.put(FIELD_UNIT_OF_MEASURE, unitOfMeasure)
            it.put(FIELD_UNIT_VALUE, unitValue)
            it.put(FIELD_BALANCE_PER_CARD, balancePerCard)
            it.put(FIELD_ON_HAND_COUNT, onHandCount)
            it.put(FIELD_REMARKS, remarks)
            it.put(FIELD_SUPPLIER, supplier)
        }
    }

    companion object {
        const val FIELD_STOCK_NUMBER = "stockNumber"
        const val FIELD_ARTICLE = "article"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_TYPE = "type"
        const val FIELD_UNIT_OF_MEASURE = "unitOfMeasure"
        const val FIELD_UNIT_VALUE = "unitValue"
        const val FIELD_BALANCE_PER_CARD = "balancePerCard"
        const val FIELD_ON_HAND_COUNT = "onHandCount"
        const val FIELD_REMARKS = "remarks"
        const val FIELD_SUPPLIER = "supplier"

        fun fromAsset(asset: Asset): InventoryItem {
            return InventoryItem(
                stockNumber = asset.stockNumber,
                article = asset.subcategory,
                description = asset.description,
                category = asset.category,
                unitOfMeasure = asset.unitOfMeasure,
                unitValue = asset.unitValue,
                remarks = asset.remarks,
            )
        }
    }
}