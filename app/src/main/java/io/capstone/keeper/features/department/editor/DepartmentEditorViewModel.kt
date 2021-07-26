package io.capstone.keeper.features.department.editor

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.features.department.Department
import io.capstone.keeper.features.department.DepartmentRepository
import io.capstone.keeper.features.shared.components.BaseViewModel
import io.capstone.keeper.features.user.User
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DepartmentEditorViewModel @Inject constructor(
    private val departmentRepository: DepartmentRepository
): BaseViewModel() {

    var department = Department()

    fun triggerManagerChanged(user: User) {
        department.managerSSN = user.minimize()
    }

    fun create() = viewModelScope.launch(IO) {
        departmentRepository.create(department)
    }
    fun update() = viewModelScope.launch(IO) {
        departmentRepository.update(department)
    }
    fun remove() = viewModelScope.launch(IO) {
        departmentRepository.remove(department)
    }
}