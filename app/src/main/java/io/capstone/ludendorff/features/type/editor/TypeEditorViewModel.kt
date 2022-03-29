package io.capstone.ludendorff.features.type.editor

import io.capstone.ludendorff.features.shared.BaseViewModel
import io.capstone.ludendorff.features.type.Type

class TypeEditorViewModel: BaseViewModel() {

    var type: Type = Type()

    fun triggerNameChanged(name: String) {
        type.typeName = name
    }

}