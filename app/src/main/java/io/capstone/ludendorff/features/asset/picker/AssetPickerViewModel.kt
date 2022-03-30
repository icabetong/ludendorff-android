package io.capstone.ludendorff.features.asset.picker

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.AssetPagingSource
import io.capstone.ludendorff.features.asset.search.AssetSearch
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AssetPickerViewModel @Inject constructor(
    firestore: FirebaseFirestore
): BaseViewModel() {
    var onSearchMode: Boolean = false

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(Asset.COLLECTION)
    private val searcher = searchHelper.searcher

    private val assetQuery: Query = firestore.collection(Asset.COLLECTION)
        .orderBy(Asset.FIELD_STOCK_NUMBER, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())

    val assets = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        AssetPagingSource(assetQuery)
    }.flow.cachedIn(viewModelScope)

    val searchResults: LiveData<PagedList<AssetSearch>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(AssetSearch.serializer())
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