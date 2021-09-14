package io.capstone.ludendorff.features.request.editor

import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.request.Request
import io.capstone.ludendorff.features.shared.components.BaseViewModel
import io.capstone.ludendorff.features.user.User
import io.capstone.ludendorff.features.user.UserCore
import javax.inject.Inject

@HiltViewModel
class RequestEditorViewModel @Inject constructor(
    private val userProperties: UserProperties,
): BaseViewModel() {

    val request: Request
        get() {
            return Request().apply {
                requestedAsset = asset?.minimize()
                petitioner = getUser()
            }
        }

    var asset: Asset? = null

    private fun getUser(): UserCore? {
        return if (userProperties.userId != null) {
            UserCore(
                userId = userProperties.userId!!,
                name = userProperties.getDisplayName(),
                email = userProperties.email,
                imageUrl = userProperties.imageUrl,
                position = userProperties.position,
                deviceToken = userProperties.deviceToken
            )
        } else null
    }
}