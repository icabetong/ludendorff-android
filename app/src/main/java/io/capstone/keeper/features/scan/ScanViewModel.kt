package io.capstone.keeper.features.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.features.asset.Asset
import io.capstone.keeper.features.asset.AssetRepository
import io.capstone.keeper.features.assignment.Assignment
import io.capstone.keeper.features.assignment.AssignmentRepository
import io.capstone.keeper.features.core.backend.Response
import io.capstone.keeper.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val assignmentRepository: AssignmentRepository,
    private val assetRepository: AssetRepository
): BaseViewModel() {
    private var decodedAssetId: String? = null

    private val _assetId = MutableLiveData<String?>(null)
    val assetId: LiveData<String?> = _assetId

    private val _assignment: MutableLiveData<Response<Assignment?>> = MutableLiveData(null)
    val assignment: LiveData<Response<Assignment?>> = _assignment

    private val _asset: MutableLiveData<Response<Asset>> = MutableLiveData(null)
    val asset: LiveData<Response<Asset>> = _asset

    fun setDecodedResult(result: String) {
        val id = result.replace(Asset.ID_PREFIX, "")
        decodedAssetId = id
        _assetId.value = id

        fetch(id)
    }

    private fun fetch(id: String) = viewModelScope.launch(IO) {
        val response = assignmentRepository.fetchUsingAssetId(id)

        /**
         *  Check if the response is successful, then
         *  try checking if an assignment with the asset id exists
         *  if it does not exist, fallback the asset document
         *  instead
         */
        if (response is Response.Success) {
            if (response.data == null) {
                _asset.postValue(assetRepository.fetch(id))
            } else _assignment.postValue(response)
        } else _assignment.postValue(response)
    }
}