package io.capstone.keeper.android.features.category

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.android.features.core.data.FirestoreLiveData
import io.capstone.keeper.android.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    var repository: FirestoreLiveData.FirestoreRepository<Category>
): BaseViewModel() {

    private var _categories: FirestoreLiveData<Category>? = repository.fetch()
    internal val categories get() = _categories

    fun fetch() {
        _categories = repository.fetch()
    }

    fun insert(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(category)
    }

    fun update(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(category)
    }

    fun remove(categoryId: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.remove(categoryId)
    }

}