package io.capstone.ludendorff.features.inventory

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
class InventoryReportViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val repository: InventoryReportRepository,
    userPreferences: UserPreferences
): BaseViewModel() {

    var sortMethod = InventoryReport.FIELD_FUND_CLUSTER
    var sortDirection = Query.Direction.ASCENDING
    var filterConstraint: String? = null
    var filterValue: String? = null

    private val inventoryQuery: Query = firestore.collection(InventoryReport.COLLECTION)
        .orderBy(InventoryReport.FIELD_FUND_CLUSTER, userPreferences.sortDirection)
        .limit(Response.QUERY_LIMIT.toLong())
    private var currentQuery = inventoryQuery

    private var pager = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        InventoryReportPagingSource(currentQuery)
    }.flow.cachedIn(viewModelScope)
    val inventoryReports = pager

    fun rebuildQuery() {
        currentQuery = firestore.collection(InventoryReport.COLLECTION)
            .orderBy(sortMethod, sortDirection)

        if (filterConstraint != null)
            currentQuery = currentQuery.whereEqualTo(filterConstraint!!, filterValue)

        currentQuery = currentQuery.limit(Response.QUERY_LIMIT.toLong())
    }

    private val _action = Channel<Response<Response.Action>>(Channel.BUFFERED)
    val action = _action.receiveAsFlow()

    fun create(inventoryReport: InventoryReport) = viewModelScope.launch(IO) {
        _action.send(repository.create(inventoryReport))
    }
    fun update(inventoryReport: InventoryReport) = viewModelScope.launch(IO) {
        _action.send(repository.update(inventoryReport))
    }
    fun remove(inventoryReport: InventoryReport) = viewModelScope.launch(IO) {
        _action.send(repository.remove(inventoryReport))
    }
}