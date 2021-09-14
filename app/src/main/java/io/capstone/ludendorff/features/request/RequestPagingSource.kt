package io.capstone.ludendorff.features.request

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import kotlinx.coroutines.tasks.await

class RequestPagingSource(
    private val requestQuery: Query
): PagingSource<QuerySnapshot, Request>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Request>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Request> {
        return try {
            val currentPage = params.key ?: requestQuery.get().await()

            if (currentPage.documents.isNotEmpty()) {
                val lastVisibleCategory = currentPage.documents[currentPage.size() - 1]
                val nextPage = requestQuery.startAfter(lastVisibleCategory).get().await()
                LoadResult.Page(
                    data = currentPage.toObjects(Request::class.java),
                    prevKey = null,
                    nextKey = nextPage
                )
            } else throw EmptySnapshotException()

        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}