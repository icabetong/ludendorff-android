package io.capstone.keeper.android.features.category.editor

import io.capstone.keeper.android.features.category.Category
import io.capstone.keeper.android.features.shared.components.BaseViewModel

class CategoryEditorViewModel: BaseViewModel() {

    var category: Category = Category()

    fun triggerNameChanged(name: String) {
        category.categoryName = name
    }

}