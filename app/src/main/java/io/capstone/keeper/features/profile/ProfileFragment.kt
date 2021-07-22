package io.capstone.keeper.features.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.work.*
import coil.load
import coil.size.Scale
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

@AndroidEntryPoint
class ProfileFragment: BaseFragment(), ProfileOptionsAdapter.ProfileOptionListener {
    private var _binding: FragmentProfileBinding? = null
    private var controller: NavController? = null
    private var optionsAdapter: ProfileOptionsAdapter? = null

    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by activityViewModels()

    private lateinit var imageRequestLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                            0)
                        if (progress == 0) {
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

        }
    }

}