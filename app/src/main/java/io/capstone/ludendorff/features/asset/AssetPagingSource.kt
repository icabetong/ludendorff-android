package io.capstone.ludendorff.features.asset

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import kotlinx.coroutines.tasks.await

class AssetPagingSource(
    private val assetQuery: Query
): PagingSource<QuerySnapshot, Asset>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Asset>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Asset> {
        return try {
            val currentPage = params.key ?: assetQuery.get().await()

            if (currentPage.documents.isNotEmpty()) {
                val lastVisibleCategory = currentPage.documents[currentPage.size() - 1]
                val nextPage = assetQuery.startAfter(lastVisibleCategory).get().await()
                LoadResult.Page(
                    data = currentPage.toObjects(Asset::class.java),
                    prevKey = null,
                    nextKey = nextPage
                )
            } else throw EmptySnapshotException()
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}