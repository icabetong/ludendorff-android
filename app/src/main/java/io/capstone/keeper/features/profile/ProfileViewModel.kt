package io.capstone.keeper.features.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.features.core.worker.ImageCompressWorker
import io.capstone.keeper.features.core.worker.ProfileUploadWorker
import io.capstone.keeper.features.shared.components.BaseViewModel
import io.capstone.keeper.features.user.User
import io.capstone.keeper.features.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val workManager: WorkManager
): BaseViewModel() {

    fun enqueueToWorkManager(request: OneTimeWorkRequest, tag: String) {
        workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request)
    }

    val compressionWorkInfo: LiveData<List<WorkInfo>> = workManager
        .getWorkInfosForUniqueWorkLiveData(ImageCompressWorker.WORKER_TAG)

    val uploadWorkInfo: LiveData<List<WorkInfo>> = workManager
        .getWorkInfosForUniqueWorkLiveData(ProfileUploadWorker.WORKER_TAG)

    fun updateProfileImage(url: String) = viewModelScope.launch(Dispatchers.IO) {
        val field = mapOf(User.FIELD_IMAGE_URL to url)

        userRepository.update()
    }
}