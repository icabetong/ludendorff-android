package io.capstone.keeper.android.features.core.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.ActivityMainBinding
import io.capstone.keeper.android.features.core.viewmodel.NavigationViewModel
import io.capstone.keeper.android.features.shared.components.BaseActivity

@AndroidEntryPoint
class MainActivity: BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}