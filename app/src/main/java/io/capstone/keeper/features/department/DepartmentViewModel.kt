package io.capstone.keeper.features.department

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.features.core.backend.FirestoreRepository
import io.capstone.keeper.features.shared.components.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class DepartmentViewModel @Inject constructor(
    firestore: FirebaseFirestore
): BaseViewModel() {

    private val departmentQuery: Query = firestore.collection(Department.COLLECTION)
        .orderBy(Department.FIELD_NAME, Query.Direction.ASCENDING)
        .limit(FirestoreRepository.QUERY_LIMIT)

    val departments = Pager(PagingConfig(pageSize = FirestoreRepository.QUERY_LIMIT.toInt())) {
        DepartmentPagingSource(departmentQuery)
    }.flow.cachedIn(viewModelScope)


}