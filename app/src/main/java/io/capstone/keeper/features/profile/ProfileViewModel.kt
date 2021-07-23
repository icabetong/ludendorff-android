package io.capstone.keeper.features.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.components.persistence.UserProperties
import io.capstone.keeper.features.core.worker.ImageCompressWorker
import io.capstone.keeper.features.core.worker.ProfileUploadWorker
import io.capstone.keeper.features.shared.components.BaseViewModel
import io.capstone.keeper.features.user.User
import io.capstone.keeper.features.user.UserRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProperties: UserProperties,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val workManager: WorkManager
): BaseViewModel() {

    enum class OperationStatus {
        IDLE, REQUESTED, COMPLETED, FAILED
    }

    private val _reauthenticationStatus = MutableLiveData(OperationStatus.IDLE)
    internal val reauthenticationStatus: LiveData<OperationStatus> = _reauthenticationStatus

    private val _passwordUpdateStatus = MutableLiveData(OperationStatus.IDLE)
    internal val passwordUpdateStatus: LiveData<OperationStatus> = _passwordUpdateStatus

    private val _linkSendingStatus = MutableLiveData(OperationStatus.IDLE)
    internal val linkSendingStatus: LiveData<OperationStatus> = _linkSendingStatus

    val firstName: String?
        get() = userProperties.firstName
    val lastName: String?
        get() = userProperties.lastName
    val fullName: String?
        get() = userProperties.getDisplayName()
    val email: String?
        get() = userProperties.email
    val permissions: Int
        get() = userProperties.permissions

    val compressionWorkInfo: LiveData<List<WorkInfo>> = workManager
        .getWorkInfosForUniqueWorkLiveData(ImageCompressWorker.WORKER_TAG)

    val uploadWorkInfo: LiveData<List<WorkInfo>> = workManager
        .getWorkInfosForUniqueWorkLiveData(ProfileUploadWorker.WORKER_TAG)

    fun endSession() = viewModelScope.launch {
        firebaseAuth.signOut()
        userProperties.clear()
    }
    fun sendPasswordResetLink(email: String?) = viewModelScope.launch {
        _linkSendingStatus.value = OperationStatus.REQUESTED
        if (email.isNullOrBlank()) {
            _linkSendingStatus.value = OperationStatus.FAILED
            return@launch
        }

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                _linkSendingStatus.value = if (it.isSuccessful) OperationStatus.COMPLETED
                    else OperationStatus.FAILED
            }
    }
    fun updatePassword(password: String?) = viewModelScope.launch {
        _passwordUpdateStatus.value = OperationStatus.REQUESTED
        if (password.isNullOrBlank()) {
            _passwordUpdateStatus.value = OperationStatus.FAILED
            return@launch
        }

        firebaseAuth.currentUser?.updatePassword(password)
            ?.addOnCompleteListener {
                _passwordUpdateStatus.value = if (it.isSuccessful) OperationStatus.COMPLETED
                    else OperationStatus.FAILED
            }
    }
    fun reauthenticate(password: String?) = viewModelScope.launch {
        _reauthenticationStatus.value = OperationStatus.REQUESTED
        val email = userProperties.email
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            _reauthenticationStatus.value = OperationStatus.FAILED
            return@launch
        }

        val credential = EmailAuthProvider.getCredential(email, password)
        firebaseAuth.currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener {
                _reauthenticationStatus.value = if (it.isSuccessful) OperationStatus.COMPLETED
                    else OperationStatus.FAILED
            }
    }
    fun resetReauthenticationStatus() {
        _reauthenticationStatus.value = OperationStatus.IDLE
    }
    fun resetPasswordUpdateStatus() {
        _passwordUpdateStatus.value = OperationStatus.IDLE
    }
    fun resetLinkSendingStatus() {
        _linkSendingStatus.value = OperationStatus.IDLE
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