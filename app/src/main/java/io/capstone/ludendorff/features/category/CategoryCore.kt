package io.capstone.ludendorff.features.category

import android.os.Parcelable
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.asset.Asset
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
@Parcelize
data class CategoryCore @JvmOverloads constructor(
    var categoryId: String = IDGenerator.generateRandom(),
    var categoryName: String? = null
): Parcelable {

    companion object {
        fun from(category: Category): CategoryCore {
            return CategoryCore(
                categoryId = category.categoryId,
                categoryName = category.categoryName
            )
        }
    }
}
