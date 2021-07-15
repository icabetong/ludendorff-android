package io.capstone.keeper.features.category

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
class CategoryViewModel @Inject constructor(
    firestore: FirebaseFirestore,
    private val repository: CategoryRepository,
): BaseViewModel() {

    private val categoryQuery: Query = firestore.collection(Category.COLLECTION)
        .orderBy(Category.FIELD_NAME, Query.Direction.ASCENDING)
        .limit(FirestoreRepository.QUERY_LIMIT)
    private val categoryPagingSource = CategoryPagingSource(categoryQuery)

    val categories = Pager(PagingConfig(pageSize = FirestoreRepository.QUERY_LIMIT.toInt())) {
        categoryPagingSource
    }.flow.cachedIn(viewModelScope)

    fun create(data: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.create(data)
    }
    fun update(data: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(data)
    }
    fun remove(id: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.remove(id)
    }
}