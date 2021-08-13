package io.capstone.ludendorff.features.assignment.editor

import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.shared.components.BaseViewModel
import io.capstone.ludendorff.features.user.User
import javax.inject.Inject

@HiltViewModel
class AssignmentEditorViewModel @Inject constructor(

): BaseViewModel() {

    var asset: Asset? = null
    var user: User? = null
}