package io.capstone.keeper.android.features.shared.components

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

abstract class BaseFragment: Fragment() {

    protected fun getParentView(): View? {
        return parentFragment?.parentFragment?.view
    }
}