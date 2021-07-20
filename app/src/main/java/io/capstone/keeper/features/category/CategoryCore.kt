package io.capstone.keeper.features.category

import android.os.Parcelable
import io.capstone.keeper.components.utils.IDGenerator
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CategoryCore @JvmOverloads constructor(
    var categoryId: String = IDGenerator.generateRandom(),
    var categoryName: String? = null
): Parcelable {

    companion object {
        fun fromCategory(category: Category): CategoryCore {
            return CategoryCore(
                categoryId = category.categoryId,
                categoryName = category.categoryName
            )
        }
    }
}
