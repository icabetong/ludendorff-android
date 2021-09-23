package io.capstone.ludendorff.features.request.sent

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.request.Request
import io.capstone.ludendorff.features.shared.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SentRequestViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
): BaseViewModel() {

    private val requestQuery: Query = firestore.collection(Request.COLLECTION)
        .whereEqualTo(Request.FIELD_PETITIONER_ID, auth.currentUser?.uid)
        .orderBy(Request.FIELD_ASSET_NAME, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())
    private var currentQuery = requestQuery

    val requests = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        SentRequestPagingSource(currentQuery)
    }.flow.cachedIn(viewModelScope)
}