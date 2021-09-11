package io.capstone.ludendorff.features.core.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.core.worker.TokenUpdateWorker
import io.capstone.ludendorff.features.shared.components.BaseViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject
import io.capstone.ludendorff.features.user.User as KeeperUser

@HiltViewModel
class CoreViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val userProperties: UserProperties
): BaseViewModel() {

    private val _userData: MutableLiveData<KeeperUser> = MutableLiveData()
    val userData: LiveData<KeeperUser> = _userData

    init {
        listenToDocumentChanges()
    }

    private fun listenToDocumentChanges() = viewModelScope.launch(IO) {
        firebaseAuth.currentUser?.uid?.let { userId ->
            firestore.collection(KeeperUser.COLLECTION)
                .document(userId)
                .addSnapshotListener { value, error ->
                    if (error != null || value == null)
                        return@addSnapshotListener

                    KeeperUser.from(value)?.let {
                        userProperties.set(it)
                        _userData.value = it
                    }
                }
        }
    }
}