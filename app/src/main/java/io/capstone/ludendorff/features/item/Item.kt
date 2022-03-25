package io.capstone.ludendorff.features.item

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item @JvmOverloads constructor(
    var stockNumber: String? = null,
    var desription: String? = null,
    var classification: String? = null,
    var type: String? = null,
    var unitOfMeasure: String? = null,
    var unitOfValue: String? = null,
    var remarks: String? = null,
): Parcelable {

}
