package io.capstone.keeper.features.category

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Category @JvmOverloads constructor(
    var categoryId: String = UUID.randomUUID().toString(),
    var categoryName: String? = null
): Parcelable {

    companion object {
        const val COLLECTION = "categories"
        const val FIELD_ID = "categoryId"
        const val FIELD_NAME = "categoryName"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem.categoryId == newItem.categoryId
            }

            override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem == newItem
            }
        }
    }
}
