package io.capstone.ludendorff.features.notification

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
): BaseViewModel() {

    private val notificationQuery = firestore.collection(Notification.COLLECTION)
        .whereEqualTo(Notification.FIELD_RECEIVER_ID, auth.currentUser?.uid)
        .orderBy(Notification.FIELD_TITLE, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())

    val notifications = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        NotificationPagingSource(notificationQuery)
    }.flow.cachedIn(viewModelScope)

}