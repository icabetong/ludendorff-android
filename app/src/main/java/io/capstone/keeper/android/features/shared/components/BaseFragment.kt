package io.capstone.keeper.android.features.shared.components

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

abstract class BaseFragment: Fragment() {

    protected fun getViewFromActivity(@IdRes id: Int): View {
        return requireActivity().findViewById(id)
    }
}