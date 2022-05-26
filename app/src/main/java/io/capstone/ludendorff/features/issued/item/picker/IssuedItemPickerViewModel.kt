package io.capstone.ludendorff.features.issued.item.picker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.issued.IssuedReport
import io.capstone.ludendorff.features.issued.IssuedReportPagingSource
import io.capstone.ludendorff.features.issued.IssuedReportRepository
import io.capstone.ludendorff.features.issued.search.IssuedReportSearch
import io.capstone.ludendorff.features.shared.BaseViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IssuedItemPickerViewModel @Inject constructor(
    firestore: FirebaseFirestore,
    private val repository: IssuedReportRepository,
): BaseViewModel() {
    var onSearchMode: Boolean = false
    var issuedReport: IssuedReport? = null
        set(value) {
            field = value
            fetch()
        }

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(IssuedReport.COLLECTION)
    private val searcher = searchHelper.searcher

    private val issuedQuery: Query = firestore.collection(IssuedReport.COLLECTION)
        .orderBy(IssuedReport.FIELD_FUND_CLUSTER, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())

    val inventories = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        IssuedReportPagingSource(issuedQuery)
    }.flow.cachedIn(viewModelScope)

    val searchResults: LiveData<PagedList<IssuedReportSearch>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(IssuedReportSearch.serializer())
        }, searchHelper.config).build()
    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(searchResults))

    private val _issuedItems = MutableLiveData<List<GroupedIssuedItem>>(mutableListOf())
    val issuedItems: LiveData<List<GroupedIssuedItem>> = _issuedItems

    init {
        connection += searchBox
    }

    private fun fetch() = viewModelScope.launch(IO) {
        issuedReport?.let {
            if (!onSearchMode) {
                val response = repository.fetch(it.issuedReportId)
                if (response is Response.Success) {
                    response.data.let { items ->
                        _issuedItems.postValue(GroupedIssuedItem.from(it.serialNumber, items))
                    }
                }
            } else _issuedItems.postValue(GroupedIssuedItem.from(it.serialNumber, it.items))
        }
    }

    override fun onCleared() {
        super.onCleared()
        connection.clear()
        searcher.cancel()
    }
}