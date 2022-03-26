package io.capstone.ludendorff.features.type

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import kotlinx.coroutines.tasks.await

class TypePagingSource(
    private val categoryQuery: Query
): PagingSource<QuerySnapshot, Type>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Type>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Type> {
        return try {
            val currentPage = params.key ?: categoryQuery.get().await()

            if (currentPage.documents.isNotEmpty()) {
                val lastVisibleCategory = currentPage.documents[currentPage.size() - 1]
                val nextPage = categoryQuery.startAfter(lastVisibleCategory).get().await()
                LoadResult.Page(
                    data = currentPage.toObjects(Type::class.java),
                    prevKey = null,
                    nextKey = nextPage
                )
            } else throw EmptySnapshotException()

        } catch(exception: Exception) {
            LoadResult.Error(exception)
        }
    }

}