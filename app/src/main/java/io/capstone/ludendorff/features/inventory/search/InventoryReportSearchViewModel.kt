package io.capstone.ludendorff.features.inventory.search

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.inventory.InventoryReport
import io.capstone.ludendorff.features.shared.BaseViewModel

class InventoryReportSearchViewModel: BaseViewModel() {

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(InventoryReport.COLLECTION)
    private val searcher = searchHelper.searcher

    val inventoryReport: LiveData<PagedList<InventoryReportSearch>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(InventoryReportSearch.serializer())
        }, searchHelper.config).build()
    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(inventoryReport))

    init {
        connection += searchBox
    }

    override fun onCleared() {
        super.onCleared()
        connection.clear()
        searcher.cancel()
    }
}