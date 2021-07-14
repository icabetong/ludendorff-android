package io.capstone.keeper.features.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.capstone.keeper.R
import io.capstone.keeper.features.shared.components.BaseViewModel

class NavigationViewModel: BaseViewModel() {

    private var _destination: MutableLiveData<Int> = MutableLiveData(R.id.navigation_user_home)
    val destination: LiveData<Int> = _destination

    fun setDestination(id: Int) {
        _destination.value = id
    }

}