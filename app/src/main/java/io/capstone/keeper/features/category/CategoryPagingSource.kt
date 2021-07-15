package io.capstone.keeper.features.category

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.keeper.components.exceptions.EmptySnapshotException
import kotlinx.coroutines.tasks.await

class CategoryPagingSource(
    private val categoryQuery: Query
): PagingSource<QuerySnapshot, Category>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Category>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Category> {
        return try {
            val currentPage = params.key ?: categoryQuery.get().await()
            try {
                val lastVisibleCategory = currentPage.documents[currentPage.size() - 1]
                val nextPage = categoryQuery.startAfter(lastVisibleCategory).get().await()
                LoadResult.Page(
                    data = currentPage.toObjects(Category::class.java),
                    prevKey = null,
                    nextKey = nextPage
                )
            } catch (arrayOutOfBounds: ArrayIndexOutOfBoundsException) {
                throw EmptySnapshotException()
            }
        } catch (emptySnapshotException: EmptySnapshotException) {
            LoadResult.Error(emptySnapshotException)
        } catch(e: Exception) {
            LoadResult.Error(e)
        }
    }

}