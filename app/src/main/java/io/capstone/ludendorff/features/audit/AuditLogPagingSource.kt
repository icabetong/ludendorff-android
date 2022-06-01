package io.capstone.ludendorff.features.audit

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import kotlinx.coroutines.tasks.await

class AuditLogPagingSource(
    private val auditLogQuery: Query
): PagingSource<QuerySnapshot, AuditLog>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, AuditLog>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, AuditLog> {
        return try {
            val currentPage = params.key ?: auditLogQuery.get().await()

            if (currentPage.documents.isNotEmpty()) {
                val lastVisibleAuditLogs = currentPage.documents[currentPage.size() - 1]
                val nextPage = auditLogQuery.startAfter(lastVisibleAuditLogs).get().await()
                LoadResult.Page(
                    data = currentPage.toObjects(AuditLog::class.java),
                    prevKey = null,
                    nextKey = nextPage,
                )
            } else throw EmptySnapshotException()

        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}