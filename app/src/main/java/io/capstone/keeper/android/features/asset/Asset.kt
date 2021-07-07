package io.capstone.keeper.android.features.asset

import java.time.ZonedDateTime
import java.util.*

data class Asset @JvmOverloads constructor(
    var assetId: String = UUID.randomUUID().toString(),
    var assetName: String? = null,
    var dateCreated: ZonedDateTime? = null,
    var status: Status? = null,
    var category: String? = null
) {

    enum class Status {
        OPERATIONAL,
        IDLE,
        UNDER_MAINTENANCE,
        RETIRED,
    }
}
