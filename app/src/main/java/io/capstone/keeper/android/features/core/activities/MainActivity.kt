package io.capstone.keeper.android.features.core.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.ActivityMainBinding
import io.capstone.keeper.android.features.auth.AuthViewModel
import io.capstone.keeper.android.features.shared.components.BaseActivity

@AndroidEntryPoint
class MainActivity: BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var controller: NavController

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller = Navigation.findNavController(this, R.id.navHostFragment)

        if (authViewModel.checkCurrentUser() != null)
            controller.navigate(R.id.to_navigation_root)
        else controller.navigate(R.id.to_navigation_auth)
    }

}