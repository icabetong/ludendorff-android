package io.capstone.ludendorff.features.category

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val repository: CategoryRepository,
): BaseViewModel() {

    private val categoryQuery: Query = firestore.collection(Category.COLLECTION)
        .orderBy(Category.FIELD_NAME, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())
    private var currentQuery = categoryQuery

    var pager = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        CategoryPagingSource(currentQuery)
    }.flow.cachedIn(viewModelScope)
    val categories = pager

    fun changeSortDirection(direction: Query.Direction) {
        currentQuery = firestore.collection(Category.COLLECTION)
            .orderBy(Category.FIELD_NAME, direction)
            .limit(Response.QUERY_LIMIT.toLong())
    }

    private val _action = Channel<Response<Response.Action>>(Channel.BUFFERED)
    val action = _action.receiveAsFlow()

    fun create(category: Category) = viewModelScope.launch(IO) {
        _action.send(repository.create(category))
    }
    fun update(category: Category) = viewModelScope.launch(IO) {
        _action.send(repository.update(category))
    }
    fun remove(category: Category) = viewModelScope.launch(IO) {
        _action.send(repository.remove(category))
    }
}