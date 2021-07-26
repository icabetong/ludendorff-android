package io.capstone.keeper.features.user.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.components.persistence.UserProperties
import io.capstone.keeper.features.core.backend.OperationStatus
import io.capstone.keeper.features.profile.ProfileViewModel
import io.capstone.keeper.features.shared.components.BaseViewModel
import io.capstone.keeper.features.user.User
import io.capstone.keeper.features.user.UserRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserEditorViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val userProperties: UserProperties
): BaseViewModel() {

    private val _reauthenticationStatus = MutableLiveData(OperationStatus.IDLE)
    internal val reauthenticationStatus: LiveData<OperationStatus> = _reauthenticationStatus

    fun reauthenticate(password: String?) = viewModelScope.launch {
        _reauthenticationStatus.value = OperationStatus.REQUESTED
        val email = userProperties.email
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            _reauthenticationStatus.value = OperationStatus.ERROR
            return@launch
        }

        val credential = EmailAuthProvider.getCredential(email, password)
        firebaseAuth.currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener {
                _reauthenticationStatus.value = if (it.isSuccessful) OperationStatus.COMPLETED
                else OperationStatus.ERROR
            }
    }
    fun setReauthenticationStatusAsIdle() {
        _reauthenticationStatus.value = OperationStatus.IDLE
    }

    var user = User()

    fun create() = viewModelScope.launch(IO) {
        userRepository.create(user)
    }
    fun update() = viewModelScope.launch(IO) {
        userRepository.update(user)
    }
}