package io.capstone.keeper.features.category.editor

import io.capstone.keeper.features.category.Category
import io.capstone.keeper.features.shared.components.BaseViewModel

class CategoryEditorViewModel: BaseViewModel() {

    var category: Category = Category()

    fun triggerNameChanged(name: String) {
        category.categoryName = name
    }

}