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

    val categories = Pager(PagingConfig(pageSize = FirestoreRepository.QUERY_LIMIT.toInt())) {
        CategoryPagingSource(categoryQuery)
    }.flow.cachedIn(viewModelScope)

    fun create(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.create(category)
    }
    fun update(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(category)
    }
    fun remove(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.remove(category)
    }
}