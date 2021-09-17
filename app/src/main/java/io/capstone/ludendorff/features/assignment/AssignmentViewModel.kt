package io.capstone.ludendorff.features.assignment

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
class AssignmentViewModel @Inject constructor(
    firestore: FirebaseFirestore,
    private val repository: AssignmentRepository
): BaseViewModel() {

    private val assignmentQuery: Query = firestore.collection(Assignment.COLLECTION)
        .orderBy(Assignment.FIELD_ASSET_NAME, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())

    val assignments = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        AssignmentPagingSource(assignmentQuery)
    }.flow.cachedIn(viewModelScope)

    private val _action = Channel<Response<Response.Action>>(Channel.BUFFERED)
    val action = _action.receiveAsFlow()

    fun create(assignment: Assignment) = viewModelScope.launch(IO) {
        _action.send(repository.create(assignment))
    }
    fun update(assignment: Assignment,
               previousUserId: String?,
               previousAssetId: String?) = viewModelScope.launch(IO) {
        _action.send(repository.update(assignment, previousUserId, previousAssetId))
    }
    fun remove(assignment: Assignment) = viewModelScope.launch(IO) {
        _action.send(repository.remove(assignment))
    }

}