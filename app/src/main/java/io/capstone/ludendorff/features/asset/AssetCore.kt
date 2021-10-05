package io.capstone.ludendorff.features.asset

import android.os.Parcelable
import androidx.annotation.Keep
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.category.CategoryCore
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 *  This data class is used in querying
 *  the minimal information the asset can be
 *  associated to.
 */
@Keep
@Serializable
@Parcelize
data class AssetCore @JvmOverloads constructor(
    var assetId: String = IDGenerator.generateRandom(),
    var assetName: String? = null,
    var status: Asset.Status? = null,
    var category: CategoryCore? = null
): Parcelable {

    companion object {
        fun from(asset: Asset): AssetCore {
            return AssetCore(
                assetId = asset.assetId,
                assetName = asset.assetName,
                status = asset.status,
                category = asset.category
            )
        }
    }
}