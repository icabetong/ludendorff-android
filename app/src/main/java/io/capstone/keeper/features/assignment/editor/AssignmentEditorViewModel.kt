package io.capstone.keeper.features.assignment.editor

import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.features.asset.Asset
import io.capstone.keeper.features.shared.components.BaseViewModel
import io.capstone.keeper.features.user.User
import javax.inject.Inject

@HiltViewModel
class AssignmentEditorViewModel @Inject constructor(

): BaseViewModel() {

    var asset: Asset? = null
    var user: User? = null
}