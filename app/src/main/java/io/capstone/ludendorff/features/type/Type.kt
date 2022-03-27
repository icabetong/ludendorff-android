package io.capstone.ludendorff.features.type

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
data class Type @JvmOverloads constructor(
    var typeId: String = IDGenerator.generateRandom(),
    var typeName: String? = null,
    var count: Int = 0,
    override val _highlightResult: @RawValue JsonObject? = null
): Parcelable, Highlightable {

    val highlightedName: HighlightedString?
        get() = getHighlight(Attribute(FIELD_NAME))

    fun minimize(): TypeCore {
        return TypeCore.from(this)
    }

    companion object {
        const val COLLECTION = "types"
        const val FIELD_ID = "typeId"
        const val FIELD_NAME = "typeName"
        const val FIELD_COUNT = "count"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Type>() {
            override fun areItemsTheSame(oldItem: Type, newItem: Type): Boolean {
                return oldItem.typeId == newItem.typeId
            }

            override fun areContentsTheSame(oldItem: Type, newItem: Type): Boolean {
                return oldItem == newItem
            }
        }
    }
}
