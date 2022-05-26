package io.capstone.ludendorff.features.inventory.picker

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.inventory.InventoryReport
import io.capstone.ludendorff.features.inventory.InventoryReportPagingSource
import io.capstone.ludendorff.features.inventory.search.InventoryReportSearch
import io.capstone.ludendorff.features.shared.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class InventoryReportPickerViewModel @Inject constructor(
    firestore: FirebaseFirestore
): BaseViewModel() {
    var onSearchMode: Boolean = false

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(InventoryReport.COLLECTION)
    private val searcher = searchHelper.searcher

    private val inventoryQuery: Query = firestore.collection(InventoryReport.COLLECTION)
        .orderBy(InventoryReport.FIELD_FUND_CLUSTER, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())

    val inventories = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        InventoryReportPagingSource(inventoryQuery)
    }.flow.cachedIn(viewModelScope)

    val searchResults: LiveData<PagedList<InventoryReportSearch>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(InventoryReportSearch.serializer())
        }, searchHelper.config).build()
    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(searchResults))

    init {
        connection += searchBox
    }

    override fun onCleared() {
        super.onCleared()
        connection.clear()
        searcher.cancel()
    }
}