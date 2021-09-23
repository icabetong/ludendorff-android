package io.capstone.ludendorff.features.request

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.BaseViewModel
import io.capstone.ludendorff.features.user.UserCore
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val repository: RequestRepository,
    private val userPreferences: UserPreferences,
    private val userProperties: UserProperties
): BaseViewModel() {

    private val requestQuery: Query = firestore.collection(Request.COLLECTION)
        .orderBy(Request.FIELD_PETITIONER, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())
    private var currentQuery = requestQuery

    val requests = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        RequestPagingSource(currentQuery)
    }.flow.cachedIn(viewModelScope)

    private val _action = Channel<Response<Response.Action>>(Channel.BUFFERED)
    val action = _action.receiveAsFlow()

    fun endorse(request: Request) = viewModelScope.launch(IO) {
        request.endorser = if (userProperties.userId != null)
            UserCore(
                userId = userProperties.userId!!,
                name = userProperties.getDisplayName(),
                email = userProperties.email,
                imageUrl = userProperties.imageUrl,
                position = userProperties.position,
                deviceToken = userProperties.deviceToken
            )
        else null

        _action.send(repository.update(request.requestId,
            mapOf(
                Request.FIELD_ENDORSER to request.endorser,
                Request.FIELD_ENDORSED_TIMESTAMP to Timestamp.now()
            ))
        )
    }

    fun create(request: Request) = viewModelScope.launch(IO) {
        _action.send(repository.create(request))
    }
    fun update(request: Request) = viewModelScope.launch(IO) {
        _action.send(repository.update(request))
    }
    fun remove(request: Request) = viewModelScope.launch(IO) {
        _action.send(repository.remove(request))
    }
}