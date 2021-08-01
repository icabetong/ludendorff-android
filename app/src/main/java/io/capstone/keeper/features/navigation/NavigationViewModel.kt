package io.capstone.keeper.features.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.R
import io.capstone.keeper.features.auth.AuthRepository
import io.capstone.keeper.features.shared.components.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val authRepository: AuthRepository
): BaseViewModel() {

    fun endSession() {
        authRepository.endSession()
    }

    private var _destination: MutableLiveData<Int> = MutableLiveData(R.id.navigation_user_home)
    val destination: LiveData<Int> = _destination

    fun setDestination(id: Int) {
        _destination.value = id
    }

}