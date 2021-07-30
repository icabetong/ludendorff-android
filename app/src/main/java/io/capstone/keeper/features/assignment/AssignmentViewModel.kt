package io.capstone.keeper.features.assignment

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.features.core.backend.Response
import io.capstone.keeper.features.shared.components.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AssignmentViewModel @Inject constructor(
    firestore: FirebaseFirestore
): BaseViewModel() {

    private val assignmentQuery: Query = firestore.collection(Assignment.COLLECTION)
        .orderBy(Assignment.FIELD_ASSET_NAME, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())

    val assignments = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        AssignmentPagingSource(assignmentQuery)
    }.flow.cachedIn(viewModelScope)

}