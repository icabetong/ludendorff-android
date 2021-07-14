package io.capstone.keeper.features.shared.components

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import io.capstone.keeper.components.persistence.UserPreferences

abstract class BaseActivity: AppCompatActivity() {

    private var toolbar: MaterialToolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        UserPreferences.notifyThemeChanged(UserPreferences(this).theme)
    }

    protected fun setPersistentActionBar(toolbar: MaterialToolbar) {
        this.toolbar = toolbar

        this.toolbar?.run {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressed() }
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    protected fun setToolbarTitle(@StringRes titleRes: Int) {
        this.toolbar?.setTitle(titleRes)
    }
}