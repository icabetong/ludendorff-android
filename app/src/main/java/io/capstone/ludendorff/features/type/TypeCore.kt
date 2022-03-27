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
    var typeId: String = IDGenerator.generateRandom(),
    var typeName: String? = null
): Parcelable {

    companion object {
        fun from(type: Type): TypeCore {
            return TypeCore(
                typeId = type.typeId,
                typeName = type.typeName
            )
        }
    }
}
