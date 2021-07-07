package io.capstone.keeper.android.features.core.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.capstone.keeper.android.R
import io.capstone.keeper.android.features.shared.components.BaseViewModel

class CoreViewModel: BaseViewModel() {

    private var _destination: MutableLiveData<Int> = MutableLiveData(R.id.navigation_user_home)
    val destination: LiveData<Int> = _destination

    fun setDestination(id: Int) {
        _destination.value = id
    }

}