package io.capstone.ludendorff.features.category.editor

import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.shared.BaseViewModel

class CategoryEditorViewModel: BaseViewModel() {

    var category: Category = Category()

    fun triggerNameChanged(name: String) {
        category.categoryName = name
    }

}