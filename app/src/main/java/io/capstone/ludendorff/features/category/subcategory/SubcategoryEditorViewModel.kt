package io.capstone.ludendorff.features.category.subcategory

import io.capstone.ludendorff.features.shared.BaseViewModel

class SubcategoryEditorViewModel: BaseViewModel() {

    var subcategoryName: String = ""

    fun triggerNameChanged(name: String) {
        subcategoryName = name
    }

}