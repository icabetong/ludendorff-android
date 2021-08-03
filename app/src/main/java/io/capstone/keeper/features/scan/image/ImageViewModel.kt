package io.capstone.keeper.features.scan.image

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.components.persistence.DevicePermissions
import io.capstone.keeper.features.core.backend.Response
import io.capstone.keeper.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    devicePermissions: DevicePermissions,
    private val imageRepository: ImageRepository
): BaseViewModel() {

    private val _images: MutableLiveData<Response<List<Uri>>> = MutableLiveData()
    val images: LiveData<Response<List<Uri>>> = _images

    init {
        if (devicePermissions.readStoragePermissionGranted)
            fetch()
    }

    private fun fetch() = viewModelScope.launch(IO) {
        _images.postValue(imageRepository.fetchImages())
    }
}