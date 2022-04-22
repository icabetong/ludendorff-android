package io.capstone.ludendorff.features.category.editor

import io.capstone.ludendorff.features.shared.BaseViewModel
import io.capstone.ludendorff.features.category.Category

class CategoryEditorViewModel: BaseViewModel() {

    var category: Category = Category()

    fun triggerNameChanged(name: String) {
        category.categoryName = name
    }

}