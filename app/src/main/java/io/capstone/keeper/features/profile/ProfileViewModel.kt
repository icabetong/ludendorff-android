package io.capstone.keeper.features.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.components.persistence.UserProperties
import io.capstone.keeper.features.core.backend.Operation
import io.capstone.keeper.features.core.worker.ImageCompressWorker
import io.capstone.keeper.features.core.worker.ProfileUploadWorker
import io.capstone.keeper.features.shared.components.BaseViewModel
import io.capstone.keeper.features.user.User
import io.capstone.keeper.features.user.UserRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProperties: UserProperties,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val workManager: WorkManager
): BaseViewModel() {

    private val _reauthentication = Channel<Operation<Nothing>>(Channel.BUFFERED)
    val reauthentication = _reauthentication.receiveAsFlow()

    private val _passwordUpdate = Channel<Operation<Nothing>>(Channel.BUFFERED)
    val passwordUpdate = _passwordUpdate.receiveAsFlow()

    private val _passwordResetEmailSent = Channel<Operation<Nothing>>(Channel.BUFFERED)
    val passwordResetEmailSent = _passwordResetEmailSent.receiveAsFlow()

    val firstName: String?
        get() = userProperties.firstName
    val lastName: String?
        get() = userProperties.lastName
    val fullName: String?
        get() = userProperties.getDisplayName()
    val email: String?
        get() = userProperties.email
    val imageUrl: String?
        get() = userProperties.imageUrl

    val compressionWorkInfo: LiveData<List<WorkInfo>> = workManager
        .getWorkInfosForUniqueWorkLiveData(ImageCompressWorker.WORKER_TAG)

    val uploadWorkInfo: LiveData<List<WorkInfo>> = workManager
        .getWorkInfosForUniqueWorkLiveData(ProfileUploadWorker.WORKER_TAG)

    fun endSession() = viewModelScope.launch {
        firebaseAuth.signOut()
        userProperties.clear()
    }
    fun sendPasswordResetLink(email: String?) = viewModelScope.launch(IO) {
        if (email.isNullOrBlank()) {
            _passwordResetEmailSent.send(Operation.Error(NullPointerException()))
            return@launch
        }

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                this.launch {
                    if (it.isSuccessful)
                        _passwordResetEmailSent.send(Operation.Success(null))
                    else _passwordResetEmailSent.send(Operation.Error(it.exception))
                }
            }.await()
    }
    fun updatePassword(password: String?) = viewModelScope.launch(IO) {
        if (password.isNullOrBlank()) {
            _passwordUpdate.send(Operation.Error(NullPointerException()))
            return@launch
        }

        firebaseAuth.currentUser?.updatePassword(password)
            ?.addOnCompleteListener {
                this.launch {
                    if (it.isSuccessful)
                        _passwordUpdate.send(Operation.Success(null))
                    else _passwordUpdate.send(Operation.Error(it.exception))
                }
            }?.await()
    }
    fun reauthenticate(password: String?) = viewModelScope.launch(IO) {
        val email = userProperties.email
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            _reauthentication.send(Operation.Error(NullPointerException()))
            return@launch
        }

        val credential = EmailAuthProvider.getCredential(email, password)
        firebaseAuth.currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener {
                this.launch {
                    if (it.isSuccessful)
                        _reauthentication.send(Operation.Success(null))
                    else _reauthentication.send(Operation.Error(it.exception))
                }
            }?.await()
    }

    /**
     *  Update the Firebase database with the new
     *  names of the user
     *  @param firstName the new user's First Name
     *  @param lastName the new user's Last Name
     */
    fun updateNames(firstName: String?, lastName: String?) = viewModelScope.launch(IO) {
        val fields = mapOf(User.FIELD_FIRST_NAME to firstName,
            User.FIELD_LAST_NAME to lastName)

        firebaseAuth.currentUser?.uid?.let {
            userRepository.update(it, fields)
        }
    }

    /**
     *  Update the Firestore database with the new
     *  url for the user's profile picture
     *  @param url the url of the user's new profile picture
     */
    fun updateProfileImage(url: String) = viewModelScope.launch(IO) {
        val fields = mapOf(User.FIELD_IMAGE_URL to url)

        firebaseAuth.currentUser?.uid?.let {
            userRepository.update(it, fields)
        }
    }

    /**
     *  Pass the work request to the WorkManager instance
     *  @param request the Work Request that needs to be done.
     *  @param tag the unique tag that will be used to identify the request
     */
    fun enqueueToWorkManager(request: OneTimeWorkRequest, tag: String) {
        workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request)
    }
}