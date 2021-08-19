package io.capstone.ludendorff.features.category

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import io.capstone.ludendorff.components.utils.IDGenerator
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category @JvmOverloads constructor(
    var categoryId: String = IDGenerator.generateRandom(),
    var categoryName: String? = null,
    var count: Int = 0
): Parcelable {

    fun minimize(): CategoryCore {
        return CategoryCore.from(this)
    }

    companion object {
        const val COLLECTION = "categories"
        const val FIELD_ID = "categoryId"
        const val FIELD_NAME = "categoryName"
        const val FIELD_COUNT = "count"

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
