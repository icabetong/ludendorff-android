package io.capstone.keeper.android.features.asset.category

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Category @JvmOverloads constructor(
    var categoryId: String = UUID.randomUUID().toString(),
    var categoryName: String? = null
): Parcelable
