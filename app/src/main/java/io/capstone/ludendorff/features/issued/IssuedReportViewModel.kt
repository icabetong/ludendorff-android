package io.capstone.ludendorff.features.issued

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
class IssuedReportViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val repository: IssuedReportRepository,
    userPreferences: UserPreferences
): BaseViewModel() {

    var sortMethod = IssuedReport.FIELD_FUND_CLUSTER
    var sortDirection = Query.Direction.ASCENDING
    var filterConstraint: String? = null
    var filterValue: String? = null

    private val inventoryQuery: Query = firestore.collection(IssuedReport.COLLECTION)
        .orderBy(IssuedReport.FIELD_FUND_CLUSTER, userPreferences.sortDirection)
        .limit(Response.QUERY_LIMIT.toLong())
    private var currentQuery = inventoryQuery

    private var pager = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        IssuedReportPagingSource(currentQuery)
    }.flow.cachedIn(viewModelScope)
    val issuedReports = pager

    fun rebuildQuery() {
        currentQuery = firestore.collection(IssuedReport.COLLECTION)
            .orderBy(sortMethod, sortDirection)

        if (filterConstraint != null)
            currentQuery = currentQuery.whereEqualTo(filterConstraint!!, filterValue)

        currentQuery = currentQuery.limit(Response.QUERY_LIMIT.toLong())
    }

    private val _action = Channel<Response<Response.Action>>(Channel.BUFFERED)
    val action = _action.receiveAsFlow()

    fun create(issuedReport: IssuedReport) = viewModelScope.launch(IO) {
        _action.send(repository.create(issuedReport))
    }
    fun update(issuedReport: IssuedReport) = viewModelScope.launch(IO) {
        _action.send(repository.update(issuedReport))
    }
    fun remove(issuedReport: IssuedReport) = viewModelScope.launch(IO) {
        _action.send(repository.remove(issuedReport))
    }
}