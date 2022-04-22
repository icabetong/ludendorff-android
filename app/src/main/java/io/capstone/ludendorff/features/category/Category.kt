package io.capstone.ludendorff.features.category

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.helper.highlighting.Highlightable
import com.algolia.search.model.Attribute
import io.capstone.ludendorff.components.utils.IDGenerator
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Keep
@Serializable
@Parcelize
data class Category @JvmOverloads constructor(
    var categoryId: String = IDGenerator.generateRandom(),
    var categoryName: String? = null,
    var subcategories: List<String> = emptyList(),
    var count: Int = 0,
    override val _highlightResult: @RawValue JsonObject? = null
): Parcelable, Highlightable {

    val highlightedName: HighlightedString?
        get() = getHighlight(Attribute(FIELD_NAME))

    fun minimize(): CategoryCore {
        return CategoryCore.from(this)
    }

    companion object {
        const val COLLECTION = "categories"
        const val FIELD_ID = "categoryId"
        const val FIELD_NAME = "categoryName"
        const val FIELD_SUBCATEGORIES = "subcategories"
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
