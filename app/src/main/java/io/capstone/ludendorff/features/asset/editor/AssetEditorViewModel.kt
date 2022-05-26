package io.capstone.ludendorff.features.asset.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.category.CategoryCore
import io.capstone.ludendorff.features.category.CategoryRepository
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetEditorViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
): BaseViewModel() {

    var asset = Asset()
    var previousCategory: CategoryCore? = null

    private val _subcategories: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    val subcategories: LiveData<List<String>> = _subcategories

    fun triggerCategoryChanged(newCategory: Category) {
        previousCategory = asset.category
        asset.category = newCategory.minimize()
        fetchSubcategories(newCategory.categoryId)
    }

    fun fetchSubcategories(categoryId: String) = viewModelScope.launch {
        val response = categoryRepository.fetchSubcategories(categoryId)
        if (response is Response.Success)
            _subcategories.value = response.data
    }

}