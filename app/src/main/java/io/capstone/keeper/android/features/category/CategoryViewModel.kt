package io.capstone.keeper.android.features.category

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.android.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
): BaseViewModel() {

    fun insert(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(category)
    }
}