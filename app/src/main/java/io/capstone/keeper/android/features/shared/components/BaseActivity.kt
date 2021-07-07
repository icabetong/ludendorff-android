package io.capstone.keeper.android.features.shared.components

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

abstract class BaseActivity: AppCompatActivity() {

    private var toolbar: MaterialToolbar? = null

    protected fun setPersistentActionBar(toolbar: MaterialToolbar?) {
        this.toolbar = toolbar

        setSupportActionBar(this.toolbar)
        this.toolbar?.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    protected fun setToolbarTitle(@StringRes id: Int) {
        this.toolbar?.title = getString(id)
    }

}