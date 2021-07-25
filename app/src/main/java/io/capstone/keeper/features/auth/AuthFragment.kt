package io.capstone.keeper.features.auth

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.util.PatternsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.components.exceptions.EmptyCredentialsException
import io.capstone.keeper.databinding.FragmentAuthBinding
import io.capstone.keeper.features.core.backend.OperationStatus
import io.capstone.keeper.features.shared.components.BaseFragment
@AndroidEntryPoint
class AuthFragment: BaseFragment() {
    private var _binding: FragmentAuthBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

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

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

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
        binding.forgotPasswordButton.setOnClickListener {
            MaterialDialog(requireContext()).show {
                lifecycleOwner(viewLifecycleOwner)
                title(R.string.dialog_send_reset_link_title)
                message(R.string.dialog_send_reset_link_message)
                input(hintRes = R.string.hint_email, waitForPositiveButton = false,
                    inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) { dialog, text ->

                    val inputField = dialog.getInputField()
                    val isValid = text.isNotEmpty()
                            && PatternsCompat.EMAIL_ADDRESS.matcher(text).matches()

                    inputField.error = if (isValid) null
                    else getString(R.string.error_invalid_email_address)
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                }
                positiveButton(R.string.button_send) {
                    val input = it.getInputField().text.toString()

                    viewModel.requestPasswordResetEmail(input)
                }
                negativeButton(R.string.button_cancel)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        setSystemBarColor(R.color.keeper_background_content)

        viewModel.currentUser.observe(viewLifecycleOwner) {
            when (it) {
                is AuthStatus.Success -> {
                    createSnackbar(R.string.feedback_sign_in_success)
                    viewModel.setUserProperties(it.user)

                    controller?.navigate(AuthFragmentDirections.toNavigationRoot())
                    binding.errorTextView.isVisible = false
                }
                is AuthStatus.Error -> {
                    resetProgress()

                    binding.errorTextView.isVisible = true
                    when(it.exception) {
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
                is AuthStatus.Authenticating -> {
                    binding.emailTextInputLayout.isEnabled = false
                    binding.passwordTextInputLayout.isEnabled = false
                    binding.authenticateButton.isEnabled = false
                    binding.authenticateButton.setText(R.string.button_authenticating)
                }
            }
        }

        viewModel.resetEmailSendingStatus.observe(viewLifecycleOwner) {
            when(it) {
                OperationStatus.REQUESTED -> {
                    binding.root.isEnabled = false
                }
                OperationStatus.COMPLETED -> {
                    binding.root.isEnabled = true
                    viewModel.setPasswordResetRequestAsIdle()
                    createSnackbar(R.string.feedback_reset_link_sent)
                }
                OperationStatus.ERROR -> {
                    viewModel.setPasswordResetRequestAsIdle()
                    binding.root.isEnabled = true
                    createSnackbar(R.string.error_generic)
                }
                else -> binding.root.isEnabled = true
            }
        }
    }

    override fun onStop() {
        super.onStop()

        setSystemBarColor(R.color.keeper_background_main)
    }

    override fun onResume() {
        super.onResume()

        binding.emailTextInput.doAfterTextChanged { resetErrors() }
        binding.passwordTextInput.doAfterTextChanged { resetErrors() }
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