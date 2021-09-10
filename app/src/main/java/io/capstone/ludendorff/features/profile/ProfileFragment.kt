package io.capstone.ludendorff.features.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.util.PatternsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.work.*
import coil.load
import coil.size.Scale
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.NavigationItemDecoration
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.databinding.FragmentProfileBinding
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.core.worker.ImageCompressWorker
import io.capstone.ludendorff.features.core.worker.ProfileUploadWorker
import io.capstone.ludendorff.features.profile.actions.ChangeNameBottomSheet
import io.capstone.ludendorff.features.profile.actions.ChangePasswordBottomSheet
import io.capstone.ludendorff.features.shared.components.BaseFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executor

@AndroidEntryPoint
class ProfileFragment: BaseFragment(), ProfileOptionsAdapter.ProfileOptionListener,
    FragmentResultListener, BaseFragment.CascadeMenuDelegate {

    private var _binding: FragmentProfileBinding? = null
    private var controller: NavController? = null
    private var optionsAdapter: ProfileOptionsAdapter? = null

    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by activityViewModels()
    private val coreViewModel: CoreViewModel by activityViewModels()

    private lateinit var imageRequestLauncher: ActivityResultLauncher<String>
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = buildContainerTransform()
        sharedElementReturnTransition = buildContainerTransform()

        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor, biometricCallback)

        imageRequestLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    val data = Data.Builder()
                        .putString(ImageCompressWorker.EXTRA_SOURCE, uri.toString())
                        .build()

                    val request = OneTimeWorkRequestBuilder<ImageCompressWorker>()
                        .setInputData(data)
                        .addTag(ImageCompressWorker.WORKER_TAG)
                        .build()

                    viewModel.enqueueToWorkManager(request, ImageCompressWorker.WORKER_TAG)
                }
            }

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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.transitionName = TRANSITION_IMAGE

        setInsets(view, binding.appBar.toolbar, binding.recyclerView)
        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_profile,
            iconRes = R.drawable.ic_hero_arrow_left,
            onNavigationClicked = { controller?.navigateUp() },
            menuRes = R.menu.menu_profile,
            onMenuOptionClicked = ::onMenuItemClicked
        )

        optionsAdapter = ProfileOptionsAdapter(requireActivity(), R.menu.menu_actions,
            this)
        with(binding.recyclerView) {
            addItemDecoration(NavigationItemDecoration(context))
            adapter = optionsAdapter
        }

        registerForFragmentResult(
            arrayOf(ChangePasswordBottomSheet.REQUEST_KEY_CHANGE,
                ChangeNameBottomSheet.REQUEST_KEY_CHANGE),
            this
        )
    }

    override fun onStart() {
        super.onStart()

        controller = findNavController()
        val progressDrawable = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 4f
            centerRadius = 24f
            setTint(ContextCompat.getColor(requireContext(), R.color.keeper_primary))
            start()
        }

        coreViewModel.userData.observe(viewLifecycleOwner) {
            binding.nameTextView.text = it.getDisplayName()
            binding.emailTextView.text = it.email
            if (it.imageUrl != null)
                binding.imageView.load(it.imageUrl) {
                    error(R.drawable.ic_hero_user)
                    placeholder(progressDrawable)
                    scale(Scale.FILL)
                }
            else binding.imageView.setImageResource(R.drawable.ic_flaticon_user)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.passwordResetEmailSent.collect {
                when(it) {
                    is Response.Error -> {
                        binding.appBarProgressIndicator.isVisible = false
                        binding.nestedScrollView.isEnabled = true

                        createSnackbar(R.string.error_generic)
                    }
                    is Response.Success -> {
                        binding.appBarProgressIndicator.isVisible = false
                        binding.nestedScrollView.isEnabled = true

                        createSnackbar(R.string.feedback_reset_link_sent)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.passwordUpdate.collect {
                when(it) {
                    is Response.Error -> {
                        binding.appBarProgressIndicator.isVisible = false
                        binding.nestedScrollView.isEnabled = true

                        createSnackbar(R.string.error_generic)
                    }
                    is Response.Success -> {
                        binding.appBarProgressIndicator.isVisible = false
                        binding.nestedScrollView.isEnabled = true

                        createSnackbar(R.string.feedback_updated_password)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reauthentication.collect {
                when(it) {
                    is Response.Error -> {
                        binding.appBarProgressIndicator.isVisible = false
                        binding.nestedScrollView.isEnabled = true

                        createSnackbar(R.string.error_invalid_credentials)
                    }
                    is Response.Success -> {
                        binding.appBarProgressIndicator.isVisible = false
                        binding.nestedScrollView.isEnabled = true

                        /**
                         *  The user have successfully authenticated
                         *  his credentials
                         */
                        ChangePasswordBottomSheet(childFragmentManager)
                            .show()
                    }
                }
            }
        }

        viewModel.compressionWorkInfo.observe(viewLifecycleOwner) { workInfo ->
            if (workInfo.isNullOrEmpty())
                return@observe

            workInfo[0].let {
                when(it.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        it.outputData.getString(ImageCompressWorker.EXTRA_IMAGE)?.let { path ->
                            val request = OneTimeWorkRequestBuilder<ProfileUploadWorker>()
                                .setInputData(workDataOf(ProfileUploadWorker.EXTRA_SOURCE to path))
                                .addTag(ProfileUploadWorker.WORKER_TAG)
                                .build()

                            viewModel.enqueueToWorkManager(request, ProfileUploadWorker.WORKER_TAG)
                            binding.progressBar.isIndeterminate = false
                        }
                    }
                    WorkInfo.State.FAILED -> {
                        binding.progressBar.hide()
                        createSnackbar(R.string.error_generic)
                    }
                    WorkInfo.State.RUNNING -> {
                        binding.progressBar.show()
                    }
                    else -> {}
                }
            }
        }

        viewModel.uploadWorkInfo.observe(viewLifecycleOwner) { workInfo ->
            if (workInfo.isNullOrEmpty())
                return@observe

            workInfo[0].let {
                when (it.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        binding.progressBar.hide()
                        it.outputData.getString(ProfileUploadWorker.EXTRA_URL)?.let { url ->
                            binding.imageView.load(url) {
                                scale(Scale.FILL)
                            }
                            viewModel.updateProfileImage(url)
                        }
                    }
                    WorkInfo.State.FAILED -> {
                        binding.progressBar.hide()
                        createSnackbar(R.string.error_generic)
                    }
                    WorkInfo.State.RUNNING -> {
                        val progress = it.progress.getInt(ProfileUploadWorker.TASK_PROGRESS,
                            -1)
                        if (progress < 0) {
                            binding.progressBar.hide()
                            binding.progressBar.isIndeterminate = true
                            binding.progressBar.show()
                        } else binding.progressBar.setProgressCompat(progress, true)
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.imageView.setOnClickListener {
            imageRequestLauncher.launch("image/*")
        }
    }

    @SuppressLint("CheckResult")
    override fun onProfileOptionSelected(id: Int) {
        when(id) {
            R.id.action_change_name -> {
                ChangeNameBottomSheet(childFragmentManager).show {
                    arguments = bundleOf(
                        ChangeNameBottomSheet.EXTRA_FIRST_NAME to viewModel.firstName,
                        ChangeNameBottomSheet.EXTRA_LAST_NAME to viewModel.lastName)
                }
            }
            R.id.action_change_password -> {
                promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.authentication_confirm))
                    .setSubtitle(getString(R.string.authentication_confirm_summary))
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG
                            or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            R.id.action_request_password_reset -> {
                MaterialDialog(requireContext()).show {
                    lifecycleOwner(viewLifecycleOwner)
                    title(R.string.dialog_send_reset_link_title)
                    message(R.string.dialog_send_reset_link_message)
                    positiveButton(R.string.button_send) {
                        viewModel.sendPasswordResetLink()
                    }
                    negativeButton(R.string.button_cancel)
                }
            }
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            ChangeNameBottomSheet.REQUEST_KEY_CHANGE -> {
                val firstName = result.getString(ChangeNameBottomSheet.EXTRA_FIRST_NAME)
                val lastName = result.getString(ChangeNameBottomSheet.EXTRA_LAST_NAME)

                viewModel.updateNames(firstName, lastName)
            }
            ChangePasswordBottomSheet.REQUEST_KEY_CHANGE -> {
                result.getString(ChangePasswordBottomSheet.EXTRA_PASSWORD)?.let {
                    binding.appBarProgressIndicator.isVisible = true
                    binding.nestedScrollView.isEnabled = false

                    viewModel.updatePassword(it)
                }
            }
        }
    }

    private var biometricCallback = object: BiometricPrompt.AuthenticationCallback() {
        @SuppressLint("CheckResult")
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)


            when (errorCode) {
                /**
                 *  The user did not setup any screen lock
                 *  verification
                 */
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

                            viewModel.reauthenticate(password)
                            binding.appBarProgressIndicator.isVisible = true
                            binding.nestedScrollView.isEnabled = false
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
                else -> {}
            }
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()

            createSnackbar(R.string.error_auth_failed)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            /**
             *  Biometric or other screen lock verification
             *  is successful, we can show the user the
             *  prompt in changing their password on
             *  the authentication infrastructure.
             */
            ChangePasswordBottomSheet(childFragmentManager).show()
        }
    }

    override fun onMenuItemClicked(id: Int) {
        when(id) {
            R.id.action_sign_out -> {
                MaterialDialog(requireContext()).show {
                    title(R.string.dialog_sign_out_title)
                    message(R.string.dialog_sign_out_message)
                    positiveButton(R.string.button_continue) {
                        viewModel.endSession()

                        controller?.navigate(R.id.to_navigation_auth)
                    }
                    negativeButton(R.string.button_cancel)
                }
            }
        }
    }

    companion object {
        const val TRANSITION_IMAGE = "transition:image"
    }
}