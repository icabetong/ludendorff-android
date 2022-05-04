package io.capstone.ludendorff.features.asset.usages

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.BaseViewModel
import io.capstone.ludendorff.features.stockcard.StockCard
import io.capstone.ludendorff.features.stockcard.StockCardPagingSource
import javax.inject.Inject

@HiltViewModel
class FindUsagesViewModel @Inject constructor(
    firestore: FirebaseFirestore,
    userPreferences: UserPreferences,
): BaseViewModel() {

    var assetStockNumber: String? = null
        set(value) {
            field = value
            buildQuery()
        }

    private var stockCardQuery: Query = firestore.collection(StockCard.COLLECTION)
        .orderBy(StockCard.FIELD_STOCK_CARD_ID, userPreferences.sortDirection)
        .limit(Response.QUERY_LIMIT.toLong())

    private var pager = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        StockCardPagingSource(stockCardQuery)
    }.flow.cachedIn(viewModelScope)
    val stockCards = pager

    private fun buildQuery() {
        stockCardQuery = stockCardQuery.whereEqualTo(StockCard.FIELD_STOCK_NUMBER, assetStockNumber)
    }

}