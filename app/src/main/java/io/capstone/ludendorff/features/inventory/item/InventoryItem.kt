package io.capstone.ludendorff.features.inventory.item

import android.os.Parcelable
import io.capstone.ludendorff.features.type.Type
import kotlinx.parcelize.Parcelize

@Parcelize
class InventoryItem @JvmOverloads constructor(
    var stockNumber: String? = null,
    var article: String? = null,
    var description: String? = null,
    var type: Type? = null,
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
    }
}