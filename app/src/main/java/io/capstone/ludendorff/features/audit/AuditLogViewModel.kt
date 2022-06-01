package io.capstone.ludendorff.features.audit

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
import javax.inject.Inject

@HiltViewModel
class AuditLogViewModel @Inject constructor(
    firestore: FirebaseFirestore,
    userPreferences: UserPreferences
): BaseViewModel() {

    private val auditLogsQuery: Query = firestore.collection(AuditLog.COLLECTION)
        .orderBy(AuditLog.FIELD_LOG_ENTRY_ID, userPreferences.sortDirection)
        .limit(Response.QUERY_LIMIT.toLong())
    private val currentQuery = auditLogsQuery

    var auditLogs = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        AuditLogPagingSource(currentQuery)
    }.flow.cachedIn(viewModelScope)
}