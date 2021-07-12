package io.capstone.keeper.android.features.assignment

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.ZonedDateTime
import java.util.*

@Parcelize
data class Tracking @JvmOverloads constructor(
    var trackingId: String = UUID.randomUUID().toString(),
    var asset: String? = null,
    var user: String? = null,
    var dateAssigned: ZonedDateTime? = null,
    var dateReturned: ZonedDateTime? = null,
    var location: String? = null,
    var remarks: Int = 0
): Parcelable
