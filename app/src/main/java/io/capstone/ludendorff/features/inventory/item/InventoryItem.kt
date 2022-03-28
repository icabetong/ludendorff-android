package io.capstone.ludendorff.features.inventory.item

import android.os.Parcelable
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.type.Type
import io.capstone.ludendorff.features.type.TypeCore
import kotlinx.parcelize.Parcelize

@Parcelize
class InventoryItem @JvmOverloads constructor(
    var stockNumber: String = "",
    var article: String? = null,
    var description: String? = null,
    var type: TypeCore? = null,
    var unitOfMeasure: String? = null,
    var unitValue: Double = 0.0,
    var balancePerCard: Int = 0,
    var onHandCount: Int = 0,
    var remarks: String? = null,
): Parcelable {

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

        fun fromAsset(asset: Asset): InventoryItem {
            return InventoryItem(
                stockNumber = asset.stockNumber,
                article = asset.classification,
                description = asset.description,
                type = asset.type,
                unitOfMeasure = asset.unitOfMeasure,
                unitValue = asset.unitValue,
                remarks = asset.remarks,
            )
        }
    }
}