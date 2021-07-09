package io.capstone.keeper.android.features.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.android.features.core.Response
import io.capstone.keeper.android.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
): BaseViewModel() {

    private var _users: MutableLiveData<Response<List<User>>> = MutableLiveData(Response.InProgress())
    internal val users: LiveData<Response<List<User>>> = _users

    init {
        fetch()
    }

    private fun fetch() = viewModelScope.launch(Dispatchers.IO) {
        _users.postValue(repository.fetchUsers())
    }
}