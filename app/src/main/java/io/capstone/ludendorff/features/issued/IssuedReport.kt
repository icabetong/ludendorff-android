package io.capstone.ludendorff.features.issued

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.issued.item.IssuedItem
import kotlinx.parcelize.Parcelize

/**
 *  Report on Supplies and Materials Issued
 */
@Keep
@Parcelize
data class IssuedReport @JvmOverloads constructor(
    var issuedReportId: String = IDGenerator.generateRandom(),
    var entityName: String? = null,
    var fundCluster: String? = null,
    var serialNumber: String? = null,
    var date: Timestamp = Timestamp.now(),
    @get:Exclude
    var items: List<IssuedItem> = emptyList(),
): Parcelable {

    companion object {
        const val COLLECTION = "issued"
        const val FIELD_ENTITY_NAME = "entityName"
        const val FIELD_REPORT_ID = "issuedReportId"
        const val FIELD_FUND_CLUSTER = "fundCluster"
        const val FIELD_SERIAL_NUMBER = "serialNumber"
        const val FIELD_DATE = "date"
        const val FIELD_ITEMS = "issuedItems"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<IssuedReport>() {
            override fun areItemsTheSame(oldItem: IssuedReport, newItem: IssuedReport): Boolean {
                return oldItem.issuedReportId == newItem.issuedReportId
            }

            override fun areContentsTheSame(oldItem: IssuedReport, newItem: IssuedReport): Boolean {
                return oldItem == newItem
            }
        }
    }
}