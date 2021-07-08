package io.capstone.keeper.android.features.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.components.exceptions.EmptyCredentialsException
import io.capstone.keeper.android.components.persistence.UserProperties
import io.capstone.keeper.android.databinding.FragmentAuthBinding
import io.capstone.keeper.android.features.core.Response
import io.capstone.keeper.android.features.shared.components.BaseFragment
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment: BaseFragment() {
    private var _binding: FragmentAuthBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    @Inject lateinit var userProperties: UserProperties

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         *  Get the host activity's NavController
         *  to navigate from here to to the
         *  RootFragment
         */
        controller = Navigation.findNavController(view)

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

    override fun onResume() {
        super.onResume()

        viewModel.currentUser.observe(viewLifecycleOwner) {
            when (it) {
                is Response.Success -> {
                    createSnackbar(R.string.feedback_sign_in_success)
                    userProperties.set(it.value)

                    controller?.navigate(AuthFragmentDirections.toNavigationRoot())
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
                    binding.authenticateButton.setText(R.string.button_authenticating)
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

        binding.authenticateButton.setText(R.string.button_sign_in)
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