package io.capstone.ludendorff.features.stockcard.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.inventory.InventoryReport
import io.capstone.ludendorff.features.inventory.item.InventoryItem
import io.capstone.ludendorff.features.shared.BaseViewModel
import io.capstone.ludendorff.features.shared.data.BalanceEntry
import io.capstone.ludendorff.features.stockcard.StockCard
import io.capstone.ludendorff.features.stockcard.StockCardRepository
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntry
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class StockCardEditorViewModel @Inject constructor(
    private val repository: StockCardRepository,
    private val firestore: FirebaseFirestore
): BaseViewModel() {

    var stockCard = StockCard()
        set(value) {
            field = value
            fetchEntries()
            _balanceEntries.value = value.balances.toMutableMap()
        }
        get() {
            field.entries
            return field
        }

    private val _balanceEntries = MutableLiveData<MutableMap<String, BalanceEntry>>(mutableMapOf())
    var balanceEntries: LiveData<MutableMap<String, BalanceEntry>> = _balanceEntries
    val balances: MutableMap<String, BalanceEntry>
        get() = _balanceEntries.value ?: mutableMapOf()

    fun modifyBalances(entry: StockCardEntry) = viewModelScope.launch(IO) {
        entry.inventoryReportSourceId?.let {
            val snapshot = firestore.collection(InventoryReport.COLLECTION)
                .document(it).collection(InventoryReport.FIELD_ITEMS)
                .whereEqualTo(Asset.FIELD_STOCK_NUMBER, stockCard.stockNumber)
                .get()
                .await()
            val inventoryItems = snapshot?.toObjects(InventoryItem::class.java) ?: emptyList()
            if (inventoryItems.isNotEmpty()) {
                val target = inventoryItems[0];
                if (target.onHandCount < entry.issueQuantity) {
                    throw IllegalStateException("")
                }

                val current = balances
                if (current.containsKey(entry.inventoryReportSourceId)) {
                    val currentEntry: BalanceEntry = current[entry.inventoryReportSourceId]
                        ?: throw IllegalStateException()

                    current[it] = BalanceEntry(
                        remaining = currentEntry.remaining - entry.issueQuantity,
                        entries = currentEntry.entries.plus(entry.stockCardEntryId to currentEntry.remaining - entry.issueQuantity)
                    )

                    val index = items.indexOfFirst { e -> e.stockCardEntryId == entry.stockCardEntryId }
                    if (index >= 0) {
                        entry.receivedQuantity = currentEntry.remaining
                        val currentItems = items.toMutableList()
                        currentItems[index] = entry
                        _entries.postValue(currentItems)
                    }
                    _balanceEntries.postValue(current)
                } else {
                    val currentEntry = BalanceEntry(remaining = target.onHandCount - entry.issueQuantity,
                        mapOf(entry.stockCardEntryId to target.onHandCount - entry.issueQuantity))
                    entry.receivedQuantity = currentEntry.remaining
                    current[it] = currentEntry
                    _balanceEntries.postValue(current)
                    val index = items.indexOfFirst { e -> e.stockCardEntryId == entry.stockCardEntryId }
                    if (index >= 0) {
                        entry.receivedQuantity = target.onHandCount
                        val currentItems = items.toMutableList()
                        currentItems[index] = entry
                        _entries.postValue(currentItems)
                    }
                }

            }
        }
    }

    private val _entries = MutableLiveData<List<StockCardEntry>>(mutableListOf())
    val entries: LiveData<List<StockCardEntry>> = _entries

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val items: List<StockCardEntry> get() {
        return _entries.value ?: mutableListOf()
    }

    fun setEntries(items: List<StockCardEntry>) {
        _entries.value = items
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


    fun clear() = onCleared()
    override fun onCleared() {
        super.onCleared()
        _entries.value = emptyList()
    }
}