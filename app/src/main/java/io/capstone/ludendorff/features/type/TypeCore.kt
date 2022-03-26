package io.capstone.ludendorff.features.type

import android.os.Parcelable
import androidx.annotation.Keep
import io.capstone.ludendorff.components.utils.IDGenerator
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Keep
@Serializable
@Parcelize
data class TypeCore @JvmOverloads constructor(
    var categoryId: String = IDGenerator.generateRandom(),
    var categoryName: String? = null
): Parcelable {

    companion object {
        fun from(type: Type): TypeCore {
            return TypeCore(
                categoryId = type.categoryId,
                categoryName = type.categoryName
            )
        }
    }
}
