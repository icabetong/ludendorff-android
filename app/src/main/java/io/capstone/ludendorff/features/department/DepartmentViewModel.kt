package io.capstone.ludendorff.features.department

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
class DepartmentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val repository: DepartmentRepository
): BaseViewModel() {

    private val departmentQuery: Query = firestore.collection(Department.COLLECTION)
        .orderBy(Department.FIELD_NAME, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())
    private var currentQuery = departmentQuery

    var pager = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        DepartmentPagingSource(currentQuery)
    }.flow.cachedIn(viewModelScope)
    val departments = pager

    fun changeSortDirection(direction: Query.Direction) {
        currentQuery = firestore.collection(Department.COLLECTION)
            .orderBy(Department.FIELD_NAME, direction)
            .limit(Response.QUERY_LIMIT.toLong())
    }

    private val _action = Channel<Response<Response.Action>>(Channel.BUFFERED)
    val action = _action.receiveAsFlow()

    fun create(department: Department) = viewModelScope.launch(IO) {
        _action.send(repository.create(department))
    }
    fun update(department: Department) = viewModelScope.launch(IO) {
        _action.send(repository.update(department))
    }
    fun remove(department: Department) = viewModelScope.launch(IO) {
        _action.send(repository.remove(department))
    }

}