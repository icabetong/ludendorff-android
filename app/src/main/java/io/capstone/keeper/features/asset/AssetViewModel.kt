package io.capstone.keeper.features.asset

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.features.core.backend.FirestoreRepository
import io.capstone.keeper.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetViewModel @Inject constructor(
    firestore: FirebaseFirestore,
    private val assetRepository: AssetRepository
): BaseViewModel() {

    private val assetQuery: Query = firestore.collection(Asset.COLLECTION)
        .orderBy(Asset.FIELD_NAME, Query.Direction.ASCENDING)
        .limit(FirestoreRepository.QUERY_LIMIT)

    val assets = Pager(PagingConfig(pageSize = FirestoreRepository.QUERY_LIMIT.toInt())) {
        AssetPagingSource(assetQuery)
    }.flow.cachedIn(viewModelScope)

    fun remove(asset: Asset) = viewModelScope.launch(Dispatchers.IO) {
        assetRepository.remove(asset)
    }

}