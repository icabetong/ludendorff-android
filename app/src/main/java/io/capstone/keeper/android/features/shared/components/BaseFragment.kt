package io.capstone.keeper.android.features.shared.components

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment: Fragment() {

    protected fun getParentView(): View? {
        return parentFragment?.parentFragment?.view
    }

    protected fun createSnackbar(@StringRes textRes: Int, length: Int = Snackbar.LENGTH_SHORT): Snackbar {
        return Snackbar.make(requireView(), textRes, length).apply {
            show()
        }
    }
}