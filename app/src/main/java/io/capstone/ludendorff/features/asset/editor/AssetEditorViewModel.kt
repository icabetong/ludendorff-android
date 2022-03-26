package io.capstone.ludendorff.features.asset.editor

import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.type.Type
import io.capstone.ludendorff.features.type.TypeCore
import io.capstone.ludendorff.features.shared.BaseViewModel

class AssetEditorViewModel: BaseViewModel() {

    var asset = Asset()
    var previousType: TypeCore? = null

    fun triggerCategoryChanged(newType: Type) {
        previousType = asset.type
        asset.type = newType.minimize()
    }

}