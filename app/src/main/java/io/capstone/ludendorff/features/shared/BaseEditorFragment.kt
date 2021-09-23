package io.capstone.ludendorff.features.shared

import android.os.Bundle
import com.google.android.material.transition.MaterialElevationScale

abstract class BaseEditorFragment: BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply {
            duration = TRANSITION_DURATION
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = TRANSITION_DURATION
        }
    }
}