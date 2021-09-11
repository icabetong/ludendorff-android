package io.capstone.ludendorff.features.home

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.features.assignment.Assignment
import kotlinx.coroutines.tasks.await

class HomePagingSource(
    private val homeQuery: Query
): PagingSource<QuerySnapshot, Assignment>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Assignment>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Assignment> {
        return try {
            val currentPage = params.key ?: homeQuery.get().await()

            if (currentPage.documents.isNotEmpty()) {
                val lastVisibleAssignment = currentPage.documents[currentPage.size() - 1]
                val nextPage = homeQuery.startAfter(lastVisibleAssignment).get().await()
                LoadResult.Page(
                    data = currentPage.toObjects(Assignment::class.java),
                    prevKey = null,
                    nextKey = nextPage
                )
            } else throw EmptySnapshotException()

        } catch (exception: EmptySnapshotException) {
            LoadResult.Error(exception)
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}