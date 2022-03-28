package io.capstone.ludendorff.features.issued

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import kotlinx.coroutines.tasks.await

class IssuedReportPagingSource(
    private val issuedReportQuery: Query
): PagingSource<QuerySnapshot, IssuedReport>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, IssuedReport>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, IssuedReport> {
        return try {
            val currentPage = params.key ?: issuedReportQuery.get().await()

            if (currentPage.documents.isNotEmpty()) {
                val lastVisibleIssuedReport = currentPage.documents[currentPage.size() - 1]
                val nextPage = issuedReportQuery.startAfter(lastVisibleIssuedReport)
                    .get().await()

                LoadResult.Page(
                    data = currentPage.toObjects(IssuedReport::class.java),
                    prevKey = null,
                    nextKey = nextPage,
                )
            } else throw EmptySnapshotException()
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}