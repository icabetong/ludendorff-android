package io.capstone.ludendorff.features.type.editor

import io.capstone.ludendorff.features.type.Type
import io.capstone.ludendorff.features.shared.BaseViewModel

class TypeEditorViewModel: BaseViewModel() {

    var type: Type = Type()

    fun triggerNameChanged(name: String) {
        type.categoryName = name
    }

}