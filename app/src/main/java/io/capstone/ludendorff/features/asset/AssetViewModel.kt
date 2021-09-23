package io.capstone.ludendorff.features.asset

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.BaseViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val repository: AssetRepository,
    userPreferences: UserPreferences,
): BaseViewModel() {

    var sortMethod = Asset.FIELD_NAME
    var sortDirection = Query.Direction.ASCENDING
    var filterConstraint: String? = null
    var filterValue: String? = null

    private val assetQuery: Query = firestore.collection(Asset.COLLECTION)
        .orderBy(Asset.FIELD_NAME, userPreferences.sortDirection)
        .limit(Response.QUERY_LIMIT.toLong())
    private var currentQuery = assetQuery

    private var pager = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        AssetPagingSource(currentQuery)
    }.flow.cachedIn(viewModelScope)
    val assets = pager

    fun rebuildQuery() {
        currentQuery = firestore.collection(Asset.COLLECTION)
            .orderBy(sortMethod, sortDirection)

        if (filterConstraint != null)
            currentQuery = currentQuery.whereEqualTo(filterConstraint!!, filterValue)

        currentQuery = currentQuery.limit(Response.QUERY_LIMIT.toLong())
    }

    private val _action = Channel<Response<Response.Action>>(Channel.BUFFERED)
    val action = _action.receiveAsFlow()

    fun create(asset: Asset) = viewModelScope.launch(IO) {
        _action.send(repository.create(asset))
    }
    fun update(asset: Asset, previousCategoryId: String? = null) = viewModelScope.launch(IO) {
        _action.send(repository.update(asset, previousCategoryId))
    }
    fun remove(asset: Asset) = viewModelScope.launch(IO) {
        _action.send(repository.remove(asset))
    }

}