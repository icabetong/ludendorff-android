package io.capstone.ludendorff.features.assignment.search

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.assignment.Assignment
import io.capstone.ludendorff.features.shared.BaseViewModel

class AssignmentSearchViewModel: BaseViewModel() {

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(Assignment.COLLECTION)
    private val searcher = searchHelper.searcher

    val assignments: LiveData<PagedList<Assignment>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(Assignment.serializer())
        }, searchHelper.config).build()
    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(assignments))

    init {
        connection += searchBox
    }

    override fun onCleared() {
        super.onCleared()
        connection.clear()
        searcher.cancel()
    }

}