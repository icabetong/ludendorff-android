package io.capstone.ludendorff.features.inventory.item

import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.shared.BaseViewModel

class InventoryItemEditorViewModel: BaseViewModel() {

    var asset = Asset()
        set(value) {
            field = value
            inventoryItem = InventoryItem.fromAsset(value)
        }
    var inventoryItem = InventoryItem()

    fun triggerBalancePerCardChanged(balancePerCard: String) {
        if (balancePerCard.isNotBlank()) {
            inventoryItem.balancePerCard = balancePerCard.toInt()
        } else {
            inventoryItem.balancePerCard = 0
        }
    }

    fun triggerOnHandCountChanged(onHandCount: String) {
        if (onHandCount.isNotBlank()) {
            inventoryItem.onHandCount = onHandCount.toInt()
        } else {
            inventoryItem.onHandCount = 0
        }
    }
}