package io.capstone.ludendorff.features.shared.data

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class OperationData @JvmOverloads constructor(
    var before: Map<String, String>? = null,
    var after: Map<String, String>? = null,
)