package io.capstone.keeper.features.profile

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.util.PatternsCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.work.*
import coil.load
import coil.size.Scale
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.components.custom.GenericItemDecoration
import io.capstone.keeper.components.custom.NavigationItemDecoration
import io.capstone.keeper.components.extensions.hide
import io.capstone.keeper.components.extensions.setup
import io.capstone.keeper.components.extensions.show
import io.capstone.keeper.components.persistence.UserProperties
import io.capstone.keeper.databinding.FragmentProfileBinding
import io.capstone.keeper.features.core.worker.ImageCompressWorker
import io.capstone.keeper.features.core.worker.ProfileUploadWorker
import io.capstone.keeper.features.shared.components.BaseFragment
import java.io.File
import java.util.*
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment: BaseFragment(), ProfileOptionsAdapter.ProfileOptionListener {
    private var _binding: FragmentProfileBinding? = null
    private var controller: NavController? = null
    private var optionsAdapter: ProfileOptionsAdapter? = null

    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by activityViewModels()

    private lateinit var imageRequestLauncher: ActivityResultLauncher<Intent>
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor, biometricCallback)


        imageRequestLauncher = registerForActivityResult(
            ActivityResultContracts
                .StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.data?.let { source ->

                    val data = Data.Builder()
                        .putString(ImageCompressWorker.EXTRA_SOURCE, source.toString())
                        .build()

                    val request = OneTimeWorkRequestBuilder<ImageCompressWorker>()
                        .setInputData(data)
                        .addTag(ImageCompressWorker.WORKER_TAG)
                        .build()

                    viewModel.enqueueToWorkManager(request, ImageCompressWorker.WORKER_TAG)
                }
            }
        }
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

        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_profile,
            iconRes = R.drawable.ic_hero_arrow_left,
            onNavigationClicked = { controller?.navigateUp() }
        )

        optionsAdapter = ProfileOptionsAdapter(
            requireActivity(), R.menu.menu_actions,
            this
        )
        with(binding.recyclerView) {
            addItemDecoration(NavigationItemDecoration(context))
            adapter = optionsAdapter
        }

        with(UserProperties(requireContext())) {
            binding.nameTextView.text = this.getDisplayName()
            binding.emailTextView.text = this.email
        }
    }

    override fun onStart() {
        super.onStart()

        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
    }

    override fun onResume() {
        super.onResume()

        binding.imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }

            imageRequestLauncher.launch(Intent.createChooser(intent,
                getString(R.string.title_select_profile_picture)))
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

    override fun onProfileOptionSelected(id: Int) {
        when(id) {
            R.id.action_change_name -> {

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
                    input(waitForPositiveButton = false) { dialog, text ->
                        val inputField = dialog.getInputField()
                        val isValid = text.isNotEmpty()
                                && PatternsCompat.EMAIL_ADDRESS.matcher(text).matches()

                        inputField.error = if (isValid) null
                            else getString(R.string.error_invalid_email_address)
                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                    }
                    positiveButton(R.string.button_send) {
                        val input = it.getInputField().text.toString()

                        viewModel.sendPasswordResetLink(input)
                    }
                    negativeButton(R.string.button_cancel)
                }
            }
            R.id.action_view_permissions -> {

            }
        }
    }


    private var biometricCallback = object: BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            android.util.Log.e("BIOMETRIC ERROR", errString.toString())
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            android.util.Log.e("BIOMETRIC FAILED", "failed")
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
        }
    }
}