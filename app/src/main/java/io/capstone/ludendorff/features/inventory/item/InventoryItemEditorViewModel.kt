package io.capstone.ludendorff.features.inventory.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.shared.BaseViewModel

class InventoryItemEditorViewModel: BaseViewModel() {

    var asset = Asset()
        set(value) {
            field = value
            inventoryItem = InventoryItem.fromAsset(value)
        }
    var inventoryItem = InventoryItem()

    private val _totalValue: MutableLiveData<Double> = MutableLiveData(0.0)
    val totalValue: LiveData<Double> = _totalValue

    fun recompute() {
        _totalValue.value = inventoryItem.balancePerCard * inventoryItem.unitValue
    }

    fun triggerUnitValue(unitValue: String) {
        if (unitValue.isNotBlank()) {
            inventoryItem.unitValue = unitValue.toDouble()
        } else {
            inventoryItem.unitValue = 0.0
        }
        recompute()
    }

    fun triggerBalancePerCardChanged(balancePerCard: String) {
        if (balancePerCard.isNotBlank()) {
            inventoryItem.balancePerCard = balancePerCard.toInt()
        } else {
            inventoryItem.balancePerCard = 0
        }
        recompute()
    }

    fun triggerOnHandCountChanged(onHandCount: String) {
        if (onHandCount.isNotBlank()) {
            inventoryItem.onHandCount = onHandCount.toInt()
        } else {
            inventoryItem.onHandCount = 0
        }
    }

    fun triggerSupplierChanged(supplier: String) {
        inventoryItem.supplier = supplier
    }
}