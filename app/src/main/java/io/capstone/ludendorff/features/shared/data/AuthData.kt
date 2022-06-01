package io.capstone.ludendorff.features.shared.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Keep
@Serializable
@Parcelize
data class AuthData @JvmOverloads constructor(
    var userId: String? = null,
    var name: String? = null,
    var email: String? = null
): Parcelable