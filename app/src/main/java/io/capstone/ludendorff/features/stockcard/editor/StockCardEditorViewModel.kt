package io.capstone.ludendorff.features.stockcard.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.BaseViewModel
import io.capstone.ludendorff.features.stockcard.StockCard
import io.capstone.ludendorff.features.stockcard.StockCardRepository
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntry
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockCardEditorViewModel @Inject constructor(
    private val repository: StockCardRepository
): BaseViewModel() {

    var stockCard = StockCard()
        set(value) {
            field = value
            fetchEntries()
        }

    private val _entries = MutableLiveData<List<StockCardEntry>>(mutableListOf())
    val entries: LiveData<List<StockCardEntry>> = _entries

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val items: List<StockCardEntry> get() {
        return _entries.value ?: mutableListOf()
    }

    private fun fetchEntries() = viewModelScope.launch(IO) {
        _isLoading.postValue(true)
        val response = repository.fetch(stockCard.stockCardId)
        if (response is Response.Success) {
            stockCard.entries = response.data
            _entries.postValue(response.data ?: emptyList())
        }

        _isLoading.postValue(false)
    }

    fun insert(entry: StockCardEntry) {
        val newItems = ArrayList(items)
        val index = newItems.indexOfFirst { it.stockCardEntryId == entry.stockCardEntryId }
        if (index < 0) {
            newItems.add(entry)
            _entries.value = newItems
        }
    }
    fun update(entry: StockCardEntry) {
        val newItems = ArrayList(items)
        val index = newItems.indexOfFirst { it.stockCardEntryId == entry.stockCardEntryId }
        if (index >= 0) {
            newItems[index] = entry
            _entries.value = newItems
        }
    }
    fun remove(entry: StockCardEntry) {
        val newItems = ArrayList(items)
        val index = newItems.indexOfFirst { it.stockCardEntryId == entry.stockCardEntryId }
        if (index >= 0) {
            newItems[index] = entry
            _entries.value = newItems
        }
    }
}