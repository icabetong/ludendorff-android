package io.capstone.ludendorff.features.type

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
class TypeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val repository: TypeRepository,
    userPreferences: UserPreferences
): BaseViewModel() {

    private val categoryQuery: Query = firestore.collection(Type.COLLECTION)
        .orderBy(Type.FIELD_NAME, userPreferences.sortDirection)
        .limit(Response.QUERY_LIMIT.toLong())
    private var currentQuery = categoryQuery

    var pager = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        TypePagingSource(currentQuery)
    }.flow.cachedIn(viewModelScope)
    val categories = pager

    fun changeSortDirection(direction: Query.Direction) {
        currentQuery = firestore.collection(Type.COLLECTION)
            .orderBy(Type.FIELD_NAME, direction)
            .limit(Response.QUERY_LIMIT.toLong())
    }

    private val _action = Channel<Response<Response.Action>>(Channel.BUFFERED)
    val action = _action.receiveAsFlow()

    fun create(type: Type) = viewModelScope.launch(IO) {
        _action.send(repository.create(type))
    }
    fun update(type: Type) = viewModelScope.launch(IO) {
        _action.send(repository.update(type))
    }
    fun remove(type: Type) = viewModelScope.launch(IO) {
        _action.send(repository.remove(type))
    }
}