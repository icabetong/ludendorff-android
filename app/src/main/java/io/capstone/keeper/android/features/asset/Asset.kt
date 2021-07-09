package io.capstone.keeper.android.features.asset

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.ZonedDateTime
import java.util.*

@Parcelize
data class Asset @JvmOverloads constructor(
    var assetId: String = UUID.randomUUID().toString(),
    var assetName: String? = null,
    var dateCreated: ZonedDateTime? = null,
    var status: Status? = null,
    var category: String? = null,
    var specifications: Map<String, String>
): Parcelable {

    enum class Status {
        OPERATIONAL,
        IDLE,
        UNDER_MAINTENANCE,
        RETIRED,
    }
}
