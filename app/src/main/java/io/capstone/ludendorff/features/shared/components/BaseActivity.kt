package io.capstone.ludendorff.features.shared.components

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.capstone.ludendorff.components.persistence.UserPreferences

abstract class BaseActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        UserPreferences.notifyThemeChanged(UserPreferences(this).theme)
    }
}