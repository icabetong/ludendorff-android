package io.capstone.keeper.features.shared.components

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.capstone.keeper.components.persistence.UserPreferences

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