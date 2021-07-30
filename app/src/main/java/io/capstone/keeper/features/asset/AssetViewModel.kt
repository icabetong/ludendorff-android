package io.capstone.keeper.features.asset

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.features.core.backend.Response
import io.capstone.keeper.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetViewModel @Inject constructor(
    firestore: FirebaseFirestore,
    private val repository: AssetRepository
): BaseViewModel() {

    private val assetQuery: Query = firestore.collection(Asset.COLLECTION)
        .orderBy(Asset.FIELD_NAME, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())

    val assets = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        AssetPagingSource(assetQuery)
    }.flow.cachedIn(viewModelScope)

    private val _action = Channel<Response<Response.Action>>(Channel.BUFFERED)
    val action = _action.receiveAsFlow()

    fun create(asset: Asset) = viewModelScope.launch(IO) {
        _action.send(repository.create(asset))
    }
    fun update(asset: Asset) = viewModelScope.launch(IO) {
        _action.send(repository.update(asset))
    }
    fun remove(asset: Asset) = viewModelScope.launch(IO) {
        _action.send(repository.remove(asset))
    }

}