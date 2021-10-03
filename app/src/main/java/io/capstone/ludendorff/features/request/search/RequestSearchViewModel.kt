package io.capstone.ludendorff.features.request.search

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.request.Request
import io.capstone.ludendorff.features.shared.BaseViewModel

class RequestSearchViewModel: BaseViewModel() {

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(Request.COLLECTION)
    private val searcher = searchHelper.searcher

    val requests: LiveData<PagedList<Request>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(Request.serializer())
        }, searchHelper.config).build()
    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(requests))

    init {
        connection += searchBox
    }

    override fun onCleared() {
        super.onCleared()
        connection.clear()
        searcher.cancel()
    }

}