package io.capstone.keeper.features.asset.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.capstone.keeper.features.asset.Asset
import io.capstone.keeper.features.category.Category
import io.capstone.keeper.features.shared.components.BaseViewModel

class AssetEditorViewModel: BaseViewModel() {

    private var _specifications = MutableLiveData(mutableListOf<Pair<String, String>>())
    internal val specifications: LiveData<MutableList<Pair<String, String>>> = _specifications

    var asset = Asset()
    var previousCategoryId: String? = null


    fun triggerCategoryChanged(newCategory: Category) {
        previousCategoryId = asset.category?.categoryId
        asset.category = newCategory.minimize()
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

}