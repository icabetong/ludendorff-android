package io.capstone.keeper.android.features.tracking

import java.time.ZonedDateTime
import java.util.*

data class Tracking @JvmOverloads constructor(
    var trackingId: String = UUID.randomUUID().toString(),
    var asset: String? = null,
    var user: String? = null,
    var dateAssigned: ZonedDateTime? = null,
    var dateReturned: ZonedDateTime? = null,
    var location: String? = null,
    var remarks: Int = 0
)
