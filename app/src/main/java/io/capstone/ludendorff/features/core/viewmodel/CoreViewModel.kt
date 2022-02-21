package io.capstone.ludendorff.features.core.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.auth.AuthRepository
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.BaseViewModel
import io.capstone.ludendorff.features.user.User
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class CoreViewModel @Inject constructor(
    private val userProperties: UserProperties,
    private val repository: AuthRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
): BaseViewModel() {

    private var snapshot: ListenerRegistration? = null

    private val networkStatusChannel = Channel<Boolean>(Channel.BUFFERED)
    val networkStatus = networkStatusChannel.receiveAsFlow()

    private val _userData: MutableLiveData<User> = MutableLiveData()
    val userData: LiveData<User> = _userData

    private val _authStatus = Channel<Response<Unit>>(Channel.BUFFERED)
    val authStatus = _authStatus.receiveAsFlow()

    private val _passwordResetEmailSent = Channel<Response<Unit>>(Channel.BUFFERED)
    val passwordResetEmail = _passwordResetEmailSent.receiveAsFlow()

    init {
        if (firebaseAuth.currentUser != null && firebaseAuth.currentUser?.isAnonymous != true)
            subscribeToDocumentChanges()
    }

    fun subscribeToDocumentChanges() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection(User.COLLECTION)
            .document(userId)
            .addSnapshotListener { value, error ->
                if (error != null || value == null)
                    return@addSnapshotListener

                val data = value.toObject(User::class.java) ?: return@addSnapshotListener
                userProperties.set(data)
                _userData.postValue(data)
            }
    }

    fun unsubscribeToDocumentChanges(onComplete: () -> Unit) {
        snapshot?.remove()
        userProperties.clear()
        firebaseAuth.currentUser?.delete()?.addOnSuccessListener {
            onComplete()
        }
    }

    fun authenticate(email: String, password: String) = viewModelScope.launch(IO) {
        if (email.isNotBlank() && password.isNotBlank()) {
            val response = repository.authenticate(email, password)
            _authStatus.send(response)
        }
    }

    fun authenticateAsGuest() = viewModelScope.launch(IO) {
        _authStatus.send(repository.authenticateAsGuest())
    }

    fun checkCurrentUser(): FirebaseUser? = repository.checkCurrentUser()

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

    fun setNetworkStatus(status: Boolean) = viewModelScope.launch {
        networkStatusChannel.send(status)
    }
}