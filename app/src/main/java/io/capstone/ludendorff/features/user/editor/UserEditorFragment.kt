package io.capstone.ludendorff.features.user.editor

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.databinding.FragmentEditorUserBinding
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.department.Department
import io.capstone.ludendorff.features.department.picker.DepartmentPickerBottomSheet
import io.capstone.ludendorff.features.shared.components.BaseEditorFragment
import io.capstone.ludendorff.features.shared.components.BaseFragment
import io.capstone.ludendorff.features.user.User
import io.capstone.ludendorff.features.user.UserViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class UserEditorFragment: BaseEditorFragment(), FragmentResultListener,
    BaseFragment.CascadeMenuDelegate {
    private var _binding: FragmentEditorUserBinding? = null
    private var controller: NavController? = null
    private var requestKey = REQUEST_KEY_CREATE

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private val binding get() = _binding!!
    private val editorViewModel: UserEditorViewModel by viewModels()
    private val viewModel: UserViewModel by activityViewModels()

    @Inject lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = buildContainerTransform()
        sharedElementReturnTransition = buildContainerTransform()

        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor, biometricCallback)

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    controller?.navigateUp()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.toolbar, arrayOf(binding.departmentTextInputLayout))

        binding.root.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_user_create,
            iconRes = R.drawable.ic_hero_x,
            onNavigationClicked = { controller?.navigateUp() },
            menuRes = R.menu.menu_editor_user,
            onMenuOptionClicked = ::onMenuItemClicked,
            customTitleView = binding.appBar.toolbarTitleTextView
        )

        arguments?.getParcelable<User>(EXTRA_USER)?.let {
            requestKey = REQUEST_KEY_UPDATE
            editorViewModel.user = it

            binding.appBar.toolbarTitleTextView.setText(R.string.title_user_update)
            binding.appBar.toolbar.menu.findItem(R.id.action_remove).isVisible =
                auth.currentUser?.uid != it.userId
            binding.appBar.toolbar.menu.findItem(R.id.action_disable).isVisible =
                auth.currentUser?.uid != it.userId && !it.disabled
            binding.appBar.toolbar.menu.findItem(R.id.action_enable).isVisible =
                auth.currentUser?.uid != it.userId && it.disabled
            binding.root.transitionName = TRANSITION_NAME_ROOT + it.userId

            binding.firstNameTextInput.setText(it.firstName)
            binding.lastNameTextInput.setText(it.lastName)
            binding.emailTextInput.setText(it.email)
            binding.positionTextInput.setText(it.position)

            it.department?.name?.let { departmentName ->
                binding.departmentTextInput.setText(departmentName)
            }

            binding.readChip.isChecked = it.hasPermission(User.PERMISSION_READ)
            binding.writeChip.isChecked = it.hasPermission(User.PERMISSION_WRITE)
            binding.deleteChip.isChecked = it.hasPermission(User.PERMISSION_DELETE)
            binding.managerUsersChip.isChecked = it.hasPermission(User.PERMISSION_MANAGE_USERS)
            binding.administrativeChip.isChecked = it.hasPermission(User.PERMISSION_ADMINISTRATIVE)
        }

        registerForFragmentResult(
            arrayOf(DepartmentPickerBottomSheet.REQUEST_KEY_PICK),
            this)
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        viewLifecycleOwner.lifecycleScope.launch {
            editorViewModel.reauthentication.collect {
                when(it) {
                    is Response.Error -> {
                        binding.root.isEnabled = true

                        if (it.throwable is FirebaseAuthInvalidCredentialsException)
                            createSnackbar(R.string.error_invalid_credentials)
                        else createSnackbar(R.string.error_auth_failed)
                    }
                    is Response.Success -> {
                        binding.root.isEnabled = true

                        if (requestKey == REQUEST_KEY_UPDATE)
                            viewModel.update(editorViewModel.user, editorViewModel.statusChanged)
                        else viewModel.create(editorViewModel.user)
                        controller?.navigateUp()
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        hideKeyboardFromCurrentFocus(binding.root)
    }

    override fun onResume() {
        super.onResume()

        binding.departmentTextInput.setOnClickListener {
            DepartmentPickerBottomSheet(childFragmentManager)
                .show()
        }
        binding.administrativeChip.setOnCheckedChangeListener { _, isChecked ->
            binding.permissionWarningCard.isVisible = isChecked
        }
        binding.departmentTextInputLayout.setEndIconOnClickListener {
            editorViewModel.user.department = null
            binding.departmentTextInput.setText(R.string.hint_not_set)
            binding.departmentTextInputLayout.endIconDrawable = null
        }

        binding.appBar.toolbarActionButton.setOnClickListener {

            editorViewModel.user.firstName = binding.firstNameTextInput.text.toString()
            editorViewModel.user.lastName = binding.lastNameTextInput.text.toString()
            editorViewModel.user.position = binding.positionTextInput.text.toString()
            editorViewModel.user.email = binding.emailTextInput.text.toString()

            val permissions = mutableListOf<Int>()
            if (binding.readChip.isChecked)
                permissions.add(User.PERMISSION_READ)
            if (binding.writeChip.isChecked)
                permissions.add(User.PERMISSION_WRITE)
            if (binding.deleteChip.isChecked)
                permissions.add(User.PERMISSION_DELETE)
            if (binding.managerUsersChip.isChecked)
                permissions.add(User.PERMISSION_MANAGE_USERS)
            if (binding.administrativeChip.isChecked)
                permissions.add(User.PERMISSION_ADMINISTRATIVE)
            
            editorViewModel.user.permissions = permissions

            if (editorViewModel.user.firstName.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_first_name)
                return@setOnClickListener
            }
            if (editorViewModel.user.lastName.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_last_name)
                return@setOnClickListener
            }
            if (editorViewModel.user.position.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_position)
                return@setOnClickListener
            }
            if (editorViewModel.user.email.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_email)
                return@setOnClickListener
            }
            if (editorViewModel.user.department == null) {
                createSnackbar(R.string.feedback_empty_department)
                return@setOnClickListener
            }

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.authentication_confirm))
                .setSubtitle(getString(R.string.authentication_confirm_summary))
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                        or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()

            biometricPrompt.authenticate(promptInfo)
            return@setOnClickListener
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            DepartmentPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Department>(DepartmentPickerBottomSheet.EXTRA_DEPARTMENT)?.let {
                    editorViewModel.user.department = it.minimize()

                    binding.departmentTextInput.setText(it.name)
                    binding.departmentTextInputLayout.setEndIconDrawable(R.drawable.ic_hero_x)
                }
            }
        }
    }

    private val biometricCallback = object: BiometricPrompt.AuthenticationCallback() {
        @SuppressLint("CheckResult")
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            when(errorCode) {
                BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.authentication_confirm)
                        message(R.string.authentication_confirm_summary)
                        input(hintRes = R.string.hint_password, waitForPositiveButton = false,
                            inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD) { dialog, text ->

                            val inputField = dialog.getInputField()
                            val passwordIsValid = text.isNotBlank()

                            inputField.error = if (passwordIsValid) null
                            else getString(R.string.error_empty_password)
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, passwordIsValid)
                        }
                        positiveButton(R.string.button_continue) {
                            val password = it.getInputField().text.toString()

                            editorViewModel.reauthenticate(password)
                        }
                        negativeButton(R.string.button_cancel)
                    }
                }
                BiometricPrompt.ERROR_LOCKOUT -> {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.error_auth_failed)
                        message(R.string.error_auth_failed_too_many_attempts)
                        positiveButton(android.R.string.ok)
                    }
                }
                BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.error_auth_failed)
                        message(R.string.error_auth_failed_too_many_attempts_permanent)
                        positiveButton(android.R.string.ok)
                    }
                }
                BiometricPrompt.ERROR_TIMEOUT -> {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.error_auth_failed)
                        message(R.string.error_auth_failed_timeout)
                        positiveButton(android.R.string.ok)
                    }
                }
                else -> { /** Other error codes; not sure if we need to handle it **/ }
            }
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()

            createSnackbar(R.string.error_auth_failed)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)

            if (requestKey == REQUEST_KEY_UPDATE)
                viewModel.update(editorViewModel.user, editorViewModel.statusChanged)
            else viewModel.create(editorViewModel.user)
            controller?.navigateUp()
        }
    }

    override fun onMenuItemClicked(id: Int) {
        when(id) {
            R.id.action_enable -> {
                if (requestKey == REQUEST_KEY_UPDATE) {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.dialog_enable_user_title)
                        message(R.string.dialog_enable_user_message)
                        positiveButton(R.string.button_enable) {
                            editorViewModel.user.disabled = false
                            editorViewModel.statusChanged = !editorViewModel.statusChanged

                            binding.informationCard.isVisible = editorViewModel.statusChanged
                            binding.informationCardText.setText(R.string.info_account_will_be_enabled)

                            binding.appBar.toolbar.menu.findItem(R.id.action_enable)
                                .isVisible = false
                            binding.appBar.toolbar.menu.findItem(R.id.action_disable)
                                .isVisible = true
                        }
                        negativeButton(R.string.button_cancel)
                    }
                }
            }
            R.id.action_disable -> {
                if (requestKey == REQUEST_KEY_UPDATE) {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.dialog_disable_user_title)
                        message(R.string.dialog_disable_user_message)
                        positiveButton(R.string.button_disable) {
                            editorViewModel.user.disabled = true
                            editorViewModel.statusChanged = !editorViewModel.statusChanged

                            binding.informationCard.isVisible = editorViewModel.statusChanged
                            binding.informationCardText.setText(R.string.info_account_will_be_disabled)

                            binding.appBar.toolbar.menu.findItem(R.id.action_enable)
                                .isVisible = true
                            binding.appBar.toolbar.menu.findItem(R.id.action_disable)
                                .isVisible = false
                        }
                        negativeButton(R.string.button_cancel)
                    }
                }
            }
            R.id.action_remove -> {
                if (requestKey == REQUEST_KEY_UPDATE) {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.dialog_remove_user_title)
                        message(R.string.dialog_remove_user_message)
                        positiveButton(R.string.button_remove) {
                            viewModel.remove(editorViewModel.user)
                            controller?.navigateUp()
                        }
                        negativeButton(R.string.button_cancel)
                    }
                }
            }
        }
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"
        const val EXTRA_USER = "extra:user"
    }
}