package io.capstone.ludendorff.features.issued.search

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.issued.IssuedReport
import io.capstone.ludendorff.features.shared.BaseViewModel

class IssuedReportSearchViewModel: BaseViewModel() {

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(IssuedReport.COLLECTION)
    private val searcher = searchHelper.searcher

    val issuedReport: LiveData<PagedList<IssuedReportSearch>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(IssuedReportSearch.serializer())
        }, searchHelper.config).build()
    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(issuedReport))

    init {
        connection += searchBox
    }

    override fun onCleared() {
        super.onCleared()
        connection.clear()
        searcher.cancel()
    }
}