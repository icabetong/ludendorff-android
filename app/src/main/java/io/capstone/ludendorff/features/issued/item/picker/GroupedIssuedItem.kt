package io.capstone.ludendorff.features.issued.item.picker

import android.os.Parcelable
import io.capstone.ludendorff.features.issued.item.IssuedItem
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntry
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupedIssuedItem @JvmOverloads constructor(
    val reference: String?,
    val stockNumber: String,
    val items: List<IssuedItem> = emptyList()
): Parcelable {

    companion object {
        fun from(reference: String?, items: List<IssuedItem>): List<GroupedIssuedItem> {
            return items.groupBy { it.stockNumber }.map { GroupedIssuedItem(reference, it.key, it.value) }
        }
    }
}