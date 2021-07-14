package io.capstone.keeper.features.settings

import android.os.Bundle
import io.capstone.keeper.R
import io.capstone.keeper.databinding.ActivitySettingsBinding
import io.capstone.keeper.features.shared.components.BaseActivity

class SettingsActivity: BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPersistentActionBar(binding.appBar.toolbar)
        setToolbarTitle(R.string.activity_settings)
    }
}