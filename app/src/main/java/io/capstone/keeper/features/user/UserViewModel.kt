package io.capstone.keeper.features.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.features.core.backend.FirestoreRepository
import io.capstone.keeper.features.core.data.Response
import io.capstone.keeper.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    firestore: FirebaseFirestore,
    private val repository: UserRepository
): BaseViewModel() {

    private val userQuery: Query = firestore.collection(User.COLLECTION)
        .orderBy("lastName", Query.Direction.ASCENDING)
        .limit(FirestoreRepository.QUERY_LIMIT)

    val users = Pager(PagingConfig(pageSize = FirestoreRepository.QUERY_LIMIT.toInt())) {
        UserPagingSource(userQuery)
    }.flow.cachedIn(viewModelScope)

}