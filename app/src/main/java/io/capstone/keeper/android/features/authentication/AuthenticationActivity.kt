package io.capstone.keeper.android.features.authentication

import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.databinding.ActivityAuthenticationBinding
import io.capstone.keeper.android.features.core.activities.MainActivity
import io.capstone.keeper.android.features.shared.components.BaseActivity

@AndroidEntryPoint
class AuthenticationActivity: BaseActivity() {
    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.authenticateButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}