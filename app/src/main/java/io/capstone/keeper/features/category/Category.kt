package io.capstone.keeper.features.category

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import io.capstone.keeper.components.utils.IDGenerator
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category @JvmOverloads constructor(
    var categoryId: String = IDGenerator.generateRandom(),
    var categoryName: String? = null,
    var count: Int = 0
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
