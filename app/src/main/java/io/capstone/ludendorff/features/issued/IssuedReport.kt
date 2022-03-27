package io.capstone.ludendorff.features.issued

import android.os.Parcelable
import com.google.firebase.Timestamp
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.issued.item.IssuedItem
import kotlinx.parcelize.Parcelize

/**
 *  Report on Supplies and Materials Issued
 */
@Parcelize
data class IssuedReport @JvmOverloads constructor(
    var issuedReportId: String = IDGenerator.generateRandom(),
    var entityName: String? = null,
    var fundCluster: String? = null,
    var serialNumber: String? = null,
    var date: Timestamp = Timestamp.now(),
    var items: List<IssuedItem> = emptyList(),
): Parcelable {

    companion object {
        const val COLLECTION = "issued"
        const val FIELD_REPORT_ID = "issuedReportId"
        const val FIELD_FUND_CLUSTER = "fundCluster"
        const val FIELD_SERIAL_NUMBER = "serialNumber"
        const val FIELD_DATE = "date"
        const val FIELD_ITEMS = "items"
    }
}