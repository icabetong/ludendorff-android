package io.capstone.keeper.android.features.auth

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.components.exceptions.EmptyCredentialsException
import io.capstone.keeper.android.components.persistence.UserProperties
import io.capstone.keeper.android.databinding.ActivityAuthBinding
import io.capstone.keeper.android.features.core.Response
import io.capstone.keeper.android.features.core.activities.MainActivity
import io.capstone.keeper.android.features.shared.components.BaseActivity
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity: BaseActivity() {
    private lateinit var binding: ActivityAuthBinding

    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var userProperties: UserProperties

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userProperties = UserProperties(this)

        binding.authenticateButton.setOnClickListener {
            viewModel.authenticate(binding.emailTextInput.text.toString(),
                binding.passwordTextInput.text.toString())
        }
        binding.passwordTextInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.authenticate(binding.emailTextInput.text.toString(),
                    binding.passwordTextInput.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
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
            when (it) {
                is Response.Success -> {
                    userProperties.set(it.value)

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    binding.errorTextView.isVisible = false
                }
                is Response.Error -> {
                    resetProgress()

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
                is Response.InProgress -> {
                    binding.emailTextInputLayout.isEnabled = false
                    binding.passwordTextInputLayout.isEnabled = false
                    binding.authenticateButton.isEnabled = false
                }
            }
        }
    }

    private fun resetProgress() {
        if (!binding.emailTextInputLayout.isEnabled)
            binding.emailTextInputLayout.isEnabled = true

        if (!binding.passwordTextInputLayout.isEnabled)
            binding.passwordTextInputLayout.isEnabled = true

        if (!binding.authenticateButton.isEnabled)
            binding.authenticateButton.isEnabled = true
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