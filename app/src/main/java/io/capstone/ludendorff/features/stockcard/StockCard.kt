package io.capstone.ludendorff.features.stockcard

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class StockCard @JvmOverloads constructor(
    var stockCardId: String? = null,
    var amountedRequested: Double = 0.0,
    var requestedItemValue: Double = 0.0,
    var amountGiven: Double = 0.0,
    var valueGiven: Double = 0.0,
    var amountOwned: Double = 0.0,
    var valueOwned: Double = 0.0,
    var locationGiven: String? = null,
    var unitOfMeasurement: String? = null,
    var date: Timestamp = Timestamp.now(),
    var reference: String? = null,
): Parcelable {

}

