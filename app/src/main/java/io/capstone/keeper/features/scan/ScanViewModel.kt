package io.capstone.keeper.features.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.features.asset.Asset
import io.capstone.keeper.features.asset.AssetRepository
import io.capstone.keeper.features.core.backend.Response
import io.capstone.keeper.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val assetRepository: AssetRepository
): BaseViewModel() {
    private var decodedAssetId: String? = null

    private val _asset: MutableLiveData<Response<Asset>> = MutableLiveData(null)
    internal val asset: LiveData<Response<Asset>> = _asset

    fun setDecodedResult(id: String) {
        decodedAssetId = id
    }

    fun fetchAssetRecord(id: String) = viewModelScope.launch(Dispatchers.IO) {
        _asset.postValue(assetRepository.fetch(id))
    }
}