package io.capstone.ludendorff.features.assignment.editor

import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.assignment.Assignment
import io.capstone.ludendorff.features.assignment.AssignmentRepository
import io.capstone.ludendorff.features.shared.components.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AssignmentEditorViewModel @Inject constructor(
    private val assignmentRepository: AssignmentRepository
): BaseViewModel() {

    var assignment = Assignment()

}