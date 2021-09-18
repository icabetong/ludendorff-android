package io.capstone.ludendorff.features.asset.picker

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.AssetPagingSource
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.components.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AssetPickerViewModel @Inject constructor(
    firestore: FirebaseFirestore
): BaseViewModel() {

    private val assetQuery: Query = firestore.collection(Asset.COLLECTION)
        .whereEqualTo(Asset.FIELD_STATUS, Asset.Status.IDLE)
        .orderBy(Asset.FIELD_NAME, Query.Direction.ASCENDING)
        .limit(Response.QUERY_LIMIT.toLong())

    val assets = Pager(PagingConfig(pageSize = Response.QUERY_LIMIT)) {
        AssetPagingSource(assetQuery)
    }.flow.cachedIn(viewModelScope)

}