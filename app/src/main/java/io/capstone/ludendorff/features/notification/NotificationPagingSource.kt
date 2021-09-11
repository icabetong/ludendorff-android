package io.capstone.ludendorff.features.notification

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import kotlinx.coroutines.tasks.await

class NotificationPagingSource(
    private val notificationQuery: Query
): PagingSource<QuerySnapshot, Notification>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Notification>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Notification> {
        return try {
            val currentPage = params.key ?: notificationQuery.get().await()

            if (currentPage.documents.isNotEmpty()) {
                val lastVisibleCategory = currentPage.documents[currentPage.size() - 1]
                val nextPage = notificationQuery.startAfter(lastVisibleCategory).get().await()
                LoadResult.Page(
                    data = currentPage.toObjects(Notification::class.java),
                    prevKey = null,
                    nextKey = nextPage
                )
            } else throw EmptySnapshotException()

        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}