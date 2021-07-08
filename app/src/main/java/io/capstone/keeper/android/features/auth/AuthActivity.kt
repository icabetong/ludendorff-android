package io.capstone.keeper.android.features.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.components.exceptions.EmptyCredentialsException
import io.capstone.keeper.android.databinding.ActivityAuthBinding
import io.capstone.keeper.android.features.core.Response
import io.capstone.keeper.android.features.core.activities.MainActivity
import io.capstone.keeper.android.features.shared.components.BaseActivity

@AndroidEntryPoint
class AuthActivity: BaseActivity() {
    private lateinit var binding: ActivityAuthBinding

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.authenticateButton.setOnClickListener {
            viewModel.authenticate(binding.emailTextInput.text.toString(),
                binding.passwordTextInput.text.toString())
        }
    }

    override fun onStart() {
        super.onStart()

        binding.emailTextInput.doAfterTextChanged {
            resetErrors()
        }
        binding.passwordTextInput.doAfterTextChanged {
            resetErrors()
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.currentUser.observe(this) {
            if (it is Response.Success) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                binding.errorTextView.isVisible = false
            } else if (it is Response.Error) {
                binding.errorTextView.isVisible = true
                when(it.throwable) {
                    is FirebaseAuthInvalidUserException ->
                        binding.errorTextView.setText(R.string.error_invalid_user)
                    is FirebaseAuthInvalidCredentialsException ->
                        binding.errorTextView.setText(R.string.error_invalid_credentials)
                    is EmptyCredentialsException ->
                        binding.errorTextView.setText(R.string.error_empty_credentials)
                    else ->
                        binding.errorTextView.setText(R.string.error_generic)
                }
                binding.passwordTextInputLayout.error = " "
                binding.emailTextInputLayout.error = " "
            }
        }
    }

    private fun resetErrors() {
        if (binding.emailTextInputLayout.error == " ")
            binding.emailTextInputLayout.error = null

        if (binding.passwordTextInputLayout.error == " ")
            binding.passwordTextInputLayout.error = null

        if (binding.errorTextView.isVisible)
            binding.errorTextView.isVisible = false
    }
}