package io.capstone.ludendorff.features.issued.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.issued.IssuedReport
import io.capstone.ludendorff.features.issued.IssuedReportRepository
import io.capstone.ludendorff.features.issued.item.IssuedItem
import io.capstone.ludendorff.features.shared.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IssuedReportEditorViewModel @Inject constructor(
    private val repository: IssuedReportRepository
): BaseViewModel() {

    var issuedReport = IssuedReport()
        set(value) {
            field = value
            fetchItems()
        }

    private val _issuedItems = MutableLiveData<List<IssuedItem>>(mutableListOf())
    val issuedItems: LiveData<List<IssuedItem>> = _issuedItems

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val items: List<IssuedItem> get() {
        return _issuedItems.value ?: mutableListOf()
    }

    private fun fetchItems() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val response = repository.fetch(issuedReport.issuedReportId)
        if (response is Response.Success) {
            issuedReport.items = response.data
            _issuedItems.postValue(response.data ?: emptyList())
        }

        _isLoading.postValue(false)
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
            newItems[index] = item
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