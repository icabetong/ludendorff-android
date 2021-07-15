package io.capstone.keeper.features.asset

import android.os.Parcelable
import io.capstone.keeper.features.category.Category
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 *  This data class is used in querying
 *  the minimal information the asset can be
 *  associated to.
 */
@Parcelize
data class AssetCore @JvmOverloads constructor(
    var assetId: String = UUID.randomUUID().toString(),
    var assetName: String? = null,
    var status: Asset.Status? = null,
    var category: Category? = null
): Parcelable