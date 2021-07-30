package io.capstone.keeper.features.department.editor

import io.capstone.keeper.features.department.Department
import io.capstone.keeper.features.shared.components.BaseViewModel
import io.capstone.keeper.features.user.User

class DepartmentEditorViewModel: BaseViewModel() {

    var department = Department()

    fun triggerManagerChanged(user: User) {
        department.managerSSN = user.minimize()
    }

}