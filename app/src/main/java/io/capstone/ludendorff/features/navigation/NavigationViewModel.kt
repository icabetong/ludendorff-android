package io.capstone.ludendorff.features.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.R
import io.capstone.ludendorff.features.auth.AuthRepository
import io.capstone.ludendorff.features.shared.components.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val authRepository: AuthRepository
): BaseViewModel() {

    fun endSession() {
        authRepository.endSession()
        _destination.value = R.id.navigation_user_home
    }

    private var _destination: MutableLiveData<Int> = MutableLiveData(R.id.navigation_user_home)
    val destination: LiveData<Int> = _destination

    fun setDestination(id: Int) {
        _destination.value = id
    }

}