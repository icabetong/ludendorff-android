package io.capstone.keeper.features.user

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.keeper.components.exceptions.EmptySnapshotException
import kotlinx.coroutines.tasks.await

class UserPagingSource(
    private val userQuery: Query
): PagingSource<QuerySnapshot, User>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, User>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, User> {
        return try {
            val currentPage = params.key ?: userQuery.get().await()

            if (currentPage.documents.isNotEmpty()) {
                val lastVisibleUser = currentPage.documents[currentPage.size() - 1]
                val nextPage = userQuery.startAfter(lastVisibleUser).get().await()
                LoadResult.Page(
                    data = currentPage.toObjects(User::class.java),
                    prevKey = null,
                    nextKey = nextPage
                )
            } else throw EmptySnapshotException()

        } catch (emptySnapshotException: EmptySnapshotException) {
            LoadResult.Error(emptySnapshotException)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

}