package io.capstone.keeper.features.asset.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.features.asset.Asset
import io.capstone.keeper.features.asset.AssetRepository
import io.capstone.keeper.features.category.Category
import io.capstone.keeper.features.shared.components.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetEditorViewModel @Inject constructor(
    private val repository: AssetRepository
): BaseViewModel() {

    private var _specifications = MutableLiveData(mutableListOf<Pair<String, String>>())
    internal val specifications: LiveData<MutableList<Pair<String, String>>> = _specifications

    var asset = Asset()
    var hasCategoryChanged: Boolean = false
    var previousCategoryId: String? = null


    fun triggerCategoryChanged(newCategory: Category) {
        hasCategoryChanged = true

        previousCategoryId = asset.category?.categoryId
        asset.category = newCategory.toCategoryCore()
    }

    fun setSpecifications(specifications: MutableList<Pair<String, String>>) {
        _specifications.value = specifications
    }
    fun getSpecifications(): MutableList<Pair<String, String>> {
        return _specifications.value ?: mutableListOf()
    }
    fun addSpecification(specification: Pair<String, String>) {
        val temp = ArrayList(getSpecifications())
        temp.add(specification)
        setSpecifications(temp)
    }
    fun updateSpecification(specification: Pair<String, String>) {
        val temp = ArrayList(getSpecifications())
        val index = temp.indexOfFirst { it.first == specification.first }
        if (index != -1)
            temp[index] = specification
        setSpecifications(temp)
    }
    fun removeSpecification(specification: Pair<String, String>) {
        val temp = ArrayList(getSpecifications())
        temp.remove(specification)
        setSpecifications(temp)
    }
    fun checkSpecificationIfExists(specification: Pair<String, String>): Boolean {
        val temp = getSpecifications()
        return temp.any { it.first.lowercase() == specification.first.lowercase() }
    }

    fun insert() = viewModelScope.launch(Dispatchers.IO) {
        asset.specifications = _specifications.value?.toMap() ?: emptyMap()
        repository.insert(asset)
    }
    fun update() = viewModelScope.launch(Dispatchers.IO) {
        asset.specifications = _specifications.value?.toMap() ?: emptyMap()
        repository.update(asset, previousCategoryId)
    }
    fun remove() = viewModelScope.launch(Dispatchers.IO) {
        repository.remove(asset)
    }

}