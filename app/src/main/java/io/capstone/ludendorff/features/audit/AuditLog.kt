package io.capstone.ludendorff.features.audit

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.Timestamp
import io.capstone.ludendorff.components.serialization.TimestampSerializer
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.shared.data.AuthData
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class AuditLog @JvmOverloads constructor(
    var logEntryId: String = IDGenerator.generateRandom(),
    var user: AuthData? = null,
    var dataType: DataType = DataType.asset,
    var identifier: String? = null,
    var operation: Operation = Operation.create,
    @Serializable(TimestampSerializer::class)
    var timestamp: Timestamp? = null,
)  {

    @Serializable
    @Parcelize
    enum class Operation(val operation: String): Parcelable {
        create("create"),
        update("update"),
        remove("remove");
    }

    @Serializable
    @Parcelize
    enum class DataType(val type: String): Parcelable {
        asset("asset"),
        inventoryReport("inventoryReport"),
        issued("issued"),
        stockCard("stockCard"),
        user("user")
    }

    companion object {
        const val COLLECTION = "logs"
        const val FIELD_LOG_ENTRY_ID = "logEntryId"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<AuditLog>() {
            override fun areItemsTheSame(oldItem: AuditLog, newItem: AuditLog): Boolean {
                return oldItem.logEntryId == newItem.logEntryId
            }

            override fun areContentsTheSame(
                oldItem: AuditLog,
                newItem: AuditLog
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}