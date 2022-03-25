package io.capstone.ludendorff.features.rsmi

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 *  Reports of Supplies and Materials Issued
 */
@Parcelize
data class RSMI @JvmOverloads constructor(
    var stockId: String? = null,
    var itemName: String? = null,
    var unitOfMeasurement: String? = null,
    var quantityIssued: Long = 0L,
    var pricePerQuantity: Double = 0.0,
    var totalPrice: Double = 0.0,
    var location: String? = null,
): Parcelable {

}
