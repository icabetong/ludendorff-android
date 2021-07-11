package io.capstone.keeper.android.features.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScanViewModel: ViewModel() {

    private val _decodeResult = MutableLiveData<String?>(null)
    internal val decodeResult: LiveData<String?> = _decodeResult

    fun setDecodeResult(result: String) {
        _decodeResult.postValue(result)
    }
}