package io.capstone.keeper.features.asset.editor

import io.capstone.keeper.features.asset.Asset
import io.capstone.keeper.features.shared.components.BaseViewModel

class AssetEditorViewModel: BaseViewModel() {

    var asset = Asset()
    val specifications = mutableMapOf<String, String>()

}