package io.capstone.keeper.features.asset

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetViewModel @Inject constructor(
    private val repository: AssetRepository
): BaseViewModel() {

    fun insert(asset: Asset) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(asset)
    }

}