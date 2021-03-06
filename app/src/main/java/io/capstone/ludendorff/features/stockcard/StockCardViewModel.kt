package io.capstone.ludendorff.features.stockcard

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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockCardViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val repository: StockCardRepository,
    userPreferences: UserPreferences
): BaseViewModel() {

    var sortMethod = StockCard.FIELD_STOCK_NUMBER
    var sortDirection = Query.Direction.ASCENDING
    var filterConstraint: String? = null
    var filterValue: String? = null

    private val stockCardQuery: Query = firestore.collection(StockCard.COLLECTION)
        .orderBy(StockCard.FIELD_STOCK_NUMBER, userPreferences.sortDirection)
        .limit(Response.QUERY_LIMIT.toLong())
    private var currentQuery = stockCardQuery

    private var pager = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        StockCardPagingSource(currentQuery)
    }.flow.cachedIn(viewModelScope)
    val stockCards = pager

    fun rebuildQuery() {
        currentQuery = firestore.collection(StockCard.COLLECTION)
            .orderBy(sortMethod, sortDirection)

        if (filterConstraint != null)
            currentQuery = currentQuery.whereEqualTo(filterConstraint!!, filterValue)

        currentQuery = currentQuery.limit(Response.QUERY_LIMIT.toLong())
    }

    private val _action = Channel<Response<Response.Action>>(Channel.BUFFERED)
    val action = _action.receiveAsFlow()

    fun create(stockCard: StockCard) = viewModelScope.launch(IO) {
        _action.send(repository.create(stockCard))
    }
    fun update(stockCard: StockCard) = viewModelScope.launch(IO) {
        _action.send(repository.update(stockCard))
    }
    fun remove(stockCard: StockCard) = viewModelScope.launch(IO) {
        _action.send(repository.remove(stockCard))
    }
}