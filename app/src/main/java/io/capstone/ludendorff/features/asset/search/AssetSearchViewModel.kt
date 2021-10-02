package io.capstone.ludendorff.features.asset.search

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.shared.BaseViewModel

class AssetSearchViewModel: BaseViewModel() {

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(Asset.COLLECTION)
    private val searcher = searchHelper.searcher

    val assets: LiveData<PagedList<Asset>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(Asset.serializer())
        }, searchHelper.config).build()
    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(assets))

    init {
        connection += searchBox
    }

    override fun onCleared() {
        super.onCleared()
        connection.clear()
        searcher.cancel()
    }
}