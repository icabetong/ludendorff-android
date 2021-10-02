package io.capstone.ludendorff.features.department.search

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.department.Department
import io.capstone.ludendorff.features.shared.BaseViewModel

class DepartmentSearchViewModel: BaseViewModel() {

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(Department.COLLECTION)
    private val searcher = searchHelper.searcher

    val departments: LiveData<PagedList<Department>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(Department.serializer())
        }, searchHelper.config).build()
    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(departments))

    init {
        connection += searchBox
    }

    override fun onCleared() {
        super.onCleared()
        connection.clear()
        searcher.cancel()
    }
}