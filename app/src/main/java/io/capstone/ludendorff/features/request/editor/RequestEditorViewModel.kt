package io.capstone.ludendorff.features.request.editor

import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.shared.BaseViewModel
import io.capstone.ludendorff.features.user.UserCore
import javax.inject.Inject

@HiltViewModel
class RequestEditorViewModel @Inject constructor(
    private var userProperties: UserProperties
): BaseViewModel() {

    var asset: Asset? = null
    val user: UserCore?
        get() {
            return if (userProperties.userId != null)
                UserCore(
                    userId = userProperties.userId!!,
                    name = userProperties.getDisplayName(),
                    email = userProperties.email,
                    imageUrl = userProperties.imageUrl,
                    position = userProperties.position,
                    deviceToken = userProperties.deviceToken
                )
            else null
        }

}