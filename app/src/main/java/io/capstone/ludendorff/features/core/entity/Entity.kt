package io.capstone.ludendorff.features.core.entity

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Entity @JvmOverloads constructor(
    var entityName: String? = null,
    var entityPosition: String? = null,
): Parcelable {

    companion object {
        const val DOCUMENT_KEY = "entity"
        const val FIELD_ENTITY_NAME = "entityName"
        const val FIELD_ENTITY_POSITION = "entityPosition"
    }
}
