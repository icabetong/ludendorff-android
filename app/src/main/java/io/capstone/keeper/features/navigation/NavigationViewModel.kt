package io.capstone.keeper.features.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.R
import io.capstone.keeper.components.persistence.UserProperties
import io.capstone.keeper.features.auth.AuthRepository
import io.capstone.keeper.features.shared.components.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProperties: UserProperties
): BaseViewModel() {

    val fullName: String?
        get() = userProperties.getDisplayName()
    val imageUrl: String?
        get() = userProperties.imageUrl

    fun endSession() {
        authRepository.endSession()
    }

    private var _destination: MutableLiveData<Int> = MutableLiveData(R.id.navigation_user_home)
    val destination: LiveData<Int> = _destination

    fun setDestination(id: Int) {
        _destination.value = id
    }

}