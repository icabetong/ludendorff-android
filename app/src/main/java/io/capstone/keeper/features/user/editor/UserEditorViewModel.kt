package io.capstone.keeper.features.user.editor

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.components.persistence.UserProperties
import io.capstone.keeper.features.core.backend.Response
import io.capstone.keeper.features.shared.components.BaseViewModel
import io.capstone.keeper.features.user.User
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserEditorViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userProperties: UserProperties
): BaseViewModel() {

    var user = User()
    var password: String? = null

    private val _reauthentication = Channel<Response<Unit>>(Channel.BUFFERED)
    val reauthentication = _reauthentication.receiveAsFlow()

    fun reauthenticate(password: String?) = viewModelScope.launch(IO) {
        val email = userProperties.email
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            _reauthentication.send(Response.Success(Unit))
            return@launch
        }

        val credential = EmailAuthProvider.getCredential(email, password)
        firebaseAuth.currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener {
                this.launch {
                    if (it.isSuccessful)
                        _reauthentication.send(Response.Success(Unit))
                    else _reauthentication.send(Response.Error(it.exception))
                }
            }?.await()
    }

}