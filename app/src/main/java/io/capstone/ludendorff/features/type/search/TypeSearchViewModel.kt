package io.capstone.ludendorff.features.type.search

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.type.Type
import io.capstone.ludendorff.features.shared.BaseViewModel

class TypeSearchViewModel: BaseViewModel() {

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(Type.COLLECTION)
    private val searcher = searchHelper.searcher

    val categories: LiveData<PagedList<Type>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(Type.serializer())
        }, searchHelper.config).build()
    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(categories))

    init {
        connection += searchBox
    }

    override fun onCleared() {
        super.onCleared()
        connection.clear()
        searcher.cancel()
    }

}