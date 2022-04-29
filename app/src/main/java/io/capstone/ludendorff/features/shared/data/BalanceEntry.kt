package io.capstone.ludendorff.features.shared.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BalanceEntry @JvmOverloads constructor(
    var remaining: Int = 0,
    var entries: Map<String, Int> = emptyMap()
): Parcelable