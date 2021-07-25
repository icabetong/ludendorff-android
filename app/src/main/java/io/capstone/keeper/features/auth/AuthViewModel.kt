package io.capstone.keeper.features.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.components.persistence.UserProperties
import io.capstone.keeper.features.core.backend.OperationStatus
import io.capstone.keeper.features.shared.components.BaseViewModel
import io.capstone.keeper.features.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userProperties: UserProperties,
    private val repository: AuthRepository,
    private val firebaseAuth: FirebaseAuth
): BaseViewModel() {

    private var _currentUser: MutableLiveData<AuthStatus> = MutableLiveData()
    internal val currentUser: LiveData<AuthStatus> = _currentUser

    private val _resetEmailSendingStatus = MutableLiveData(OperationStatus.IDLE)
    internal val resetEmailSendingStatus: LiveData<OperationStatus> = _resetEmailSendingStatus

    fun authenticate(email: String, password: String) = viewModelScope.launch {
        _currentUser.value = AuthStatus.Authenticating()

        if (email.isNotBlank() && password.isNotBlank()) {
            val response = repository.authenticate(email, password)
            _currentUser.postValue(response)
        }
    }

    fun checkCurrentUser(): FirebaseUser? = repository.checkCurrentUser()

    fun setUserProperties(user: User) {
        userProperties.set(user)
    }
    fun requestPasswordResetEmail(email: String?) = viewModelScope.launch(Dispatchers.IO) {
        _resetEmailSendingStatus.postValue(OperationStatus.REQUESTED)
        if (email.isNullOrBlank()) {
            _resetEmailSendingStatus.postValue(OperationStatus.ERROR)
            return@launch
        }

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                _resetEmailSendingStatus.postValue(if (it.isSuccessful) OperationStatus.COMPLETED
                else OperationStatus.ERROR)
                android.util.Log.e("DEBUG", it.exception.toString())
            }
    }
    fun setPasswordResetRequestAsIdle() {
        _resetEmailSendingStatus.value = OperationStatus.IDLE
    }

}