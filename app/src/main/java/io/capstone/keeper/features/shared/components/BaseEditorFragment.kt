package io.capstone.keeper.features.shared.components

import android.os.Bundle
import com.google.android.material.transition.MaterialElevationScale
import io.capstone.keeper.R

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

    override fun onStart() {
        super.onStart()
        setSystemBarColor(R.color.keeper_background_content)
    }

    override fun onStop() {
        super.onStop()
        setSystemBarColor(R.color.keeper_background_main)
    }

}