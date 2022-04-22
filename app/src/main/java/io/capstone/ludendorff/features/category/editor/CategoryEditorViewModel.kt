package io.capstone.ludendorff.features.category.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.shared.BaseViewModel

class CategoryEditorViewModel: BaseViewModel() {

    var category = Category()
        set(value) {
            field = value
            _subcategories.value = value.subcategories
        }

    private val _subcategories = MutableLiveData<List<String>>(mutableListOf())
    val subcategories: LiveData<List<String>> = _subcategories

    val items: List<String> get() {
        return _subcategories.value ?: mutableListOf()
    }

    fun triggerCategoryName(name: String) {
        category.categoryName = name
    }

    fun insert(subcategory: String) {
        val newItems = ArrayList(items)
        val index = newItems.indexOf(subcategory)
        if (index < 0) {
            newItems.add(subcategory)
            _subcategories.value = ArrayList(newItems)
        }
    }

    fun update(subcategory: String) {
        val newItems = ArrayList(items)
        val index = newItems.indexOf(subcategory)
        if (index >= 0) {
            newItems[index] = subcategory
            _subcategories.value = newItems
        }
    }

    fun remove(subcategory: String) {
        val newItems = ArrayList(items)
        val index = newItems.indexOf(subcategory)
        newItems.remove(subcategory)
        _subcategories.value = newItems

    }
}