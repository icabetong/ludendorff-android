package io.capstone.ludendorff.features.inventory.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.inventory.InventoryReport
import io.capstone.ludendorff.features.inventory.InventoryReportRepository
import io.capstone.ludendorff.features.inventory.item.InventoryItem
import io.capstone.ludendorff.features.shared.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryReportEditorViewModel @Inject constructor(
    private val repository: InventoryReportRepository
): BaseViewModel() {

    var inventoryReport = InventoryReport()
        set(value) {
            field = value
            fetchItems()
        }

    private val _inventoryItems = MutableLiveData<List<InventoryItem>>(mutableListOf())
    val inventoryItems: LiveData<List<InventoryItem>> = _inventoryItems

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val items: List<InventoryItem> get() {
        return _inventoryItems.value ?: mutableListOf()
    }

    private fun fetchItems() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val response = repository.fetch(inventoryReport.inventoryReportId)
        if (response is Response.Success) {
            inventoryReport.items = response.data
            _inventoryItems.postValue(response.data ?: emptyList())
        }

        _isLoading.postValue(false)
    }

    fun insert(item: InventoryItem) {
        val newItems = ArrayList(items)
        val index = newItems.indexOfFirst { it.stockNumber == item.stockNumber }
        if (index < 0) {
            newItems.add(item)
            _inventoryItems.value = ArrayList(newItems)
        }
    }
    fun update(item: InventoryItem) {
        val newItems = ArrayList(items)
        val index = newItems.indexOfFirst { it.stockNumber == item.stockNumber }
        if (index >= 0) {
            newItems[index] = item
            _inventoryItems.value = newItems
        }
    }
    fun remove(item: InventoryItem) {
        val newItems = ArrayList(items)
        val index = newItems.indexOfFirst { it.stockNumber == item.stockNumber }
        if (index >= 0) {
            newItems.removeAt(index)
            _inventoryItems.value = newItems
        }
    }

}