package io.capstone.ludendorff.features.assignment.editor

import io.capstone.ludendorff.features.assignment.Assignment
import io.capstone.ludendorff.features.shared.components.BaseViewModel

class AssignmentEditorViewModel: BaseViewModel() {

    var assignment = Assignment()
    var targetUserDeviceToken: String? = null
    var previousUserId: String? = null
    var previousAssetId: String? = null
    var shouldEndAssignment: Boolean = false

}