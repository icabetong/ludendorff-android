package io.capstone.ludendorff.features.inventory

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import kotlinx.coroutines.tasks.await

class InventoryReportPagingSource(
    private val inventoryReportQuery: Query
): PagingSource<QuerySnapshot, InventoryReport>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, InventoryReport>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, InventoryReport> {
        return try {
            val currentPage = params.key ?: inventoryReportQuery.get().await()

            if (currentPage.documents.isNotEmpty()) {
                val lastVisibleInventoryReport = currentPage.documents[currentPage.size() - 1]
                val nextPage = inventoryReportQuery.startAfter(lastVisibleInventoryReport)
                    .get().await()

                LoadResult.Page(
                    data = currentPage.toObjects(InventoryReport::class.java),
                    prevKey = null,
                    nextKey = nextPage
                )
            } else throw EmptySnapshotException()
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}