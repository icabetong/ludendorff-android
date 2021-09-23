package io.capstone.ludendorff.features.department.editor

import io.capstone.ludendorff.features.department.Department
import io.capstone.ludendorff.features.shared.BaseViewModel
import io.capstone.ludendorff.features.user.User

class DepartmentEditorViewModel: BaseViewModel() {

    var department = Department()

    fun triggerManagerChanged(user: User) {
        department.manager = user.minimize()
    }

}