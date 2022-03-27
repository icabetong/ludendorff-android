package io.capstone.ludendorff.features.issued.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.capstone.ludendorff.features.issued.IssuedReport
import io.capstone.ludendorff.features.issued.item.IssuedItem
import io.capstone.ludendorff.features.shared.BaseViewModel

class IssuedReportEditorViewModel: BaseViewModel() {

    var issuedReport = IssuedReport()

    private val _issuedItems = MutableLiveData<List<IssuedItem>>(mutableListOf())
    val issuedItems: LiveData<List<IssuedItem>> = _issuedItems

    private val items: List<IssuedItem> get() {
        return _issuedItems.value ?: mutableListOf()
    }

    fun insert(item: IssuedItem) {
        val newItems = ArrayList(items)
        val index = newItems.indexOfFirst { it.stockNumber == item.stockNumber }
        if (index < 0) {
            newItems.add(item)
            _issuedItems.value = ArrayList(newItems)
        }
    }
    fun update(item: IssuedItem) {
        val newItems = ArrayList(items)
        _issuedItems.value = mutableListOf()
        val index = newItems.indexOfFirst { it.stockNumber == item.stockNumber }
        if (index >= 0) {
            newItems[index] = item.copy()
            _issuedItems.value = newItems
        }
    }
    fun remove(item: IssuedItem) {
        val newItems = ArrayList(items)
        val index = newItems.indexOfFirst { it.stockNumber == item.stockNumber }
        if (index >= 0) {
            newItems.removeAt(index)
            _issuedItems.value = newItems
        }
    }
}