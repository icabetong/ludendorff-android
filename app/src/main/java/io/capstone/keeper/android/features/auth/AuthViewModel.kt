package io.capstone.keeper.android.features.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.android.features.core.data.Response
import io.capstone.keeper.android.features.shared.components.BaseViewModel
import io.capstone.keeper.android.features.user.User
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
): BaseViewModel() {

    private var _currentUser: MutableLiveData<AuthStatus> = MutableLiveData()
    internal val currentUser: LiveData<AuthStatus> = _currentUser

    fun authenticate(email: String, password: String) = viewModelScope.launch {
        _currentUser.value = AuthStatus.Authenticating()

        if (email.isNotBlank() && password.isNotBlank()) {
            val response = repository.authenticate(email, password)
            _currentUser.postValue(response)
        }
    }

    fun endSession() = repository.endSession()
    fun checkCurrentUser(): FirebaseUser? = repository.checkCurrentUser()

}