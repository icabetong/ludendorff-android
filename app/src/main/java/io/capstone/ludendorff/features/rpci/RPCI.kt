package io.capstone.ludendorff.features.rpci

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class RPCI @JvmOverloads constructor(
    var stockId: String? = null,
    var article: String? = null,
    var description: String? = null,
    var unitOfMeasurement: String? = null,
    var balancePerCard: Double = 0.0,
    var onHandPerCount: Int = 0,
    var totalAmount: Double = 0.0,
    var remarks: String? = null,
): Parcelable {
}