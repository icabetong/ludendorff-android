package io.capstone.ludendorff.features.auth

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.components.BaseViewModel
import io.capstone.ludendorff.features.user.User
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userProperties: UserProperties,
    private val repository: AuthRepository,
    private val firebaseAuth: FirebaseAuth
): BaseViewModel() {

    private val _authStatus = Channel<Response<User>>(Channel.BUFFERED)
    val authStatus = _authStatus.receiveAsFlow()

    private val _passwordResetEmailSent = Channel<Response<Unit>>(Channel.BUFFERED)
    val passwordResetEmail = _passwordResetEmailSent.receiveAsFlow()

    fun authenticate(email: String, password: String) = viewModelScope.launch(IO) {
        if (email.isNotBlank() && password.isNotBlank()) {
            val response = repository.authenticate(email, password)
            _authStatus.send(response)
        }
    }

    fun checkCurrentUser(): FirebaseUser? = repository.checkCurrentUser()

    fun setUserProperties(user: User) {
        userProperties.set(user)
    }
    fun requestPasswordResetEmail(email: String?) = viewModelScope.launch(IO) {
        if (email.isNullOrBlank()) {
            _passwordResetEmailSent.send(Response.Error(NullPointerException()))
            return@launch
        }

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                this.launch {
                    if (it.isSuccessful)
                        _passwordResetEmailSent.send(Response.Success(Unit))
                    else _passwordResetEmailSent.send(Response.Error(it.exception))
                }
            }.await()
    }
}