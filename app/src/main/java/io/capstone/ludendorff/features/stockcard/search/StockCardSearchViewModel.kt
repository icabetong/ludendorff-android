package io.capstone.ludendorff.features.stockcard.search

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.shared.BaseViewModel
import io.capstone.ludendorff.features.stockcard.StockCard

class StockCardSearchViewModel: BaseViewModel() {

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(StockCard.COLLECTION)
    private val searcher = searchHelper.searcher

    val stockCards: LiveData<PagedList<StockCardSearch>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(StockCardSearch.serializer())
        }, searchHelper.config).build()
    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(stockCards))

    init {
        connection += searchBox
    }

    override fun onCleared() {
        super.onCleared()
        connection.clear()
        searcher.cancel()
    }
}