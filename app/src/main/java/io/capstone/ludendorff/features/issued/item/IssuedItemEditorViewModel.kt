package io.capstone.ludendorff.features.issued.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.shared.BaseViewModel

class IssuedItemEditorViewModel: BaseViewModel() {

    var asset = Asset()
        set(value) {
            field = value
            issuedItem = IssuedItem.fromAsset(value)
        }
    var issuedItem = IssuedItem()

    private val _amount: MutableLiveData<Double> = MutableLiveData(0.0)
    val amount: LiveData<Double> = _amount

    fun recompute() {
        _amount.value = issuedItem.quantityIssued * issuedItem.unitCost
    }

    fun triggerUnitCostChanged(unitCost: String) {
        if (unitCost.isNotBlank()) {
            issuedItem.unitCost = unitCost.toDouble()
        } else {
            issuedItem.unitCost = 0.0
        }
        recompute()
    }

    fun triggerQuantityIssuedChanged(quantity: String) {
        if (quantity.isNotBlank()) {
            issuedItem.quantityIssued = quantity.toInt()
        } else {
            issuedItem.quantityIssued = 0
        }
        recompute()
    }

    fun triggerResponsibilityCenterChanged(responsibilityCenter: String) {
        issuedItem.responsibilityCenter = responsibilityCenter
    }

}