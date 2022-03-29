package io.capstone.ludendorff.features.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.core.worker.ImageCompressWorker
import io.capstone.ludendorff.features.core.worker.ProfileUploadWorker
import io.capstone.ludendorff.features.shared.BaseViewModel
import io.capstone.ludendorff.features.user.User
import io.capstone.ludendorff.features.user.UserRepository
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

    private val _reauthentication = Channel<Response<Unit>>(Channel.BUFFERED)
    val reauthentication = _reauthentication.receiveAsFlow()

    private val _passwordUpdate = Channel<Response<Unit>>(Channel.BUFFERED)
    val passwordUpdate = _passwordUpdate.receiveAsFlow()

    private val _passwordResetEmailSent = Channel<Response<Unit>>(Channel.BUFFERED)
    val passwordResetEmailSent = _passwordResetEmailSent.receiveAsFlow()

    val firstName: String?
        get() = userProperties.firstName
    val lastName: String?
        get() = userProperties.lastName

    val compressionWorkInfo: LiveData<List<WorkInfo>> = workManager
        .getWorkInfosForUniqueWorkLiveData(ImageCompressWorker.WORKER_TAG)

    val uploadWorkInfo: LiveData<List<WorkInfo>> = workManager
        .getWorkInfosForUniqueWorkLiveData(ProfileUploadWorker.WORKER_TAG)

    fun sendPasswordResetLink() = viewModelScope.launch(IO) {
        val email = firebaseAuth.currentUser?.email
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
    fun updatePassword(password: String?) = viewModelScope.launch(IO) {
        if (password.isNullOrBlank()) {
            _passwordUpdate.send(Response.Error(NullPointerException()))
            return@launch
        }

        firebaseAuth.currentUser?.updatePassword(password)
            ?.addOnCompleteListener {
                this.launch {
                    if (it.isSuccessful)
                        _passwordUpdate.send(Response.Success(Unit))
                    else _passwordUpdate.send(Response.Error(it.exception))
                }
            }?.await()
    }
    fun reauthenticate(password: String?) = viewModelScope.launch(IO) {
        val email = userProperties.email
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            _reauthentication.send(Response.Error(NullPointerException()))
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