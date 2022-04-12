package io.capstone.ludendorff.features.shared

import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.navigation.NavController
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar

abstract class BasePreference: PreferenceFragmentCompat() {

    fun createSnackbar(@StringRes id: Int, length: Int = Snackbar.LENGTH_SHORT): Snackbar {
        return Snackbar.make(requireView(), id, length).apply { show() }
    }
}