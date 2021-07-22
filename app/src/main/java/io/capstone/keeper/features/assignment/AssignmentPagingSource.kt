package io.capstone.keeper.features.assignment

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.keeper.components.exceptions.EmptySnapshotException
import kotlinx.coroutines.tasks.await

class AssignmentPagingSource(
    private val assignmentQuery: Query
): PagingSource<QuerySnapshot, Assignment>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Assignment>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Assignment> {
        return try {
            val currentPage = params.key ?: assignmentQuery.get().await()

            if (currentPage.documents.isNotEmpty()) {
                val lastVisibleCategory = currentPage.documents[currentPage.size() - 1]
                val nextPage = assignmentQuery.startAfter(lastVisibleCategory).get().await()
                LoadResult.Page(
                    data = currentPage.toObjects(Assignment::class.java),
                    prevKey = null,
                    nextKey = nextPage
                )
            } else throw EmptySnapshotException()
        } catch (firestoreException: FirebaseFirestoreException) {
            LoadResult.Error(firestoreException)
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}