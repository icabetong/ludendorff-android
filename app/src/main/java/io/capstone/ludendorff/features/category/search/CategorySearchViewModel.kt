package io.capstone.ludendorff.features.category.search

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.shared.BaseViewModel

class CategorySearchViewModel: BaseViewModel() {

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(Category.COLLECTION)
    private val searcher = searchHelper.searcher

    val categories: LiveData<PagedList<Category>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(Category.serializer())
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