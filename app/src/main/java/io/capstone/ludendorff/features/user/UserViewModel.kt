package io.capstone.ludendorff.features.user

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    firestore: FirebaseFirestore,
    private val repository: UserRepository
): BaseViewModel() {

    private val userQuery: Query = firestore.collection(User.COLLECTION)
        .orderBy(User.FIELD_LAST_NAME, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())

    val users = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        UserPagingSource(userQuery)
    }.flow.cachedIn(viewModelScope)

    private val _action = Channel<Response<Response.Action>>(Channel.BUFFERED)
    val action = _action.receiveAsFlow()

    fun create(user: User) = viewModelScope.launch(IO) {
        _action.send(repository.create(user))
    }
    fun update(user: User, statusChanged: Boolean = false) = viewModelScope.launch(IO) {
        _action.send(repository.update(user, statusChanged))
    }
    fun remove(user: User) = viewModelScope.launch(IO) {
        _action.send(repository.remove(user))
    }

}