package io.capstone.ludendorff.features.home

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.assignment.Assignment
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.components.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
): BaseViewModel() {

    private val homeQuery: Query = firestore.collection(Assignment.COLLECTION)
        .whereEqualTo(Assignment.FIELD_USER_ID, auth.currentUser?.uid)
        .orderBy(Assignment.FIELD_ASSET_NAME, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())

    val assignments = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        HomePagingSource(homeQuery)
    }.flow.cachedIn(viewModelScope)
}