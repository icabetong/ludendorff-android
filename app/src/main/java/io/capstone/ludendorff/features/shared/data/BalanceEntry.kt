package io.capstone.ludendorff.features.shared.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class BalanceEntry @JvmOverloads constructor(
    var remaining: Int = 0,
    var entries: Map<String, Int> = emptyMap()
): Parcelable {

    fun containsEntryId(entryId: String?): Boolean {
        return entries.keys.contains(entryId);
    }
}