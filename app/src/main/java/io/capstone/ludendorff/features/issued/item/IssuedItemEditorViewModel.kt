package io.capstone.ludendorff.features.issued.item

import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.shared.BaseViewModel

class IssuedItemEditorViewModel: BaseViewModel() {

    var asset = Asset()
        set(value) {
            field = value
            issuedItem = IssuedItem.fromAsset(value)
        }
    var issuedItem = IssuedItem()

    fun triggerQuantityIssuedChanged(quantity: String) {
        if (quantity.isNotBlank()) {
            issuedItem.quantityIssued = quantity.toInt()
        } else {
            issuedItem.quantityIssued = 0
        }
    }

    fun triggerResponsibilityCenterChanged(responsibilityCenter: String) {
        issuedItem.responsibilityCenter = responsibilityCenter
    }

}