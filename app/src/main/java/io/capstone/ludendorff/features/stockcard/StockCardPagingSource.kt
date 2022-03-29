package io.capstone.ludendorff.features.stockcard

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import kotlinx.coroutines.tasks.await

class StockCardPagingSource(
    private val stockCardQuery: Query
): PagingSource<QuerySnapshot, StockCard>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, StockCard>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, StockCard> {
        return try {
            val currentPage = params.key ?: stockCardQuery.get().await()

            if (currentPage.documents.isNotEmpty()) {
                val lastVisibleStockCard = currentPage.documents[currentPage.size() - 1]
                val nextPage = stockCardQuery.startAfter(lastVisibleStockCard)
                    .get().await()

                LoadResult.Page(
                    data = currentPage.toObjects(StockCard::class.java),
                    prevKey = null,
                    nextKey = nextPage,
                )
            } else throw EmptySnapshotException()
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}