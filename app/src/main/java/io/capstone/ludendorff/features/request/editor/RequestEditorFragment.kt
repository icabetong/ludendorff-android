package io.capstone.ludendorff.features.request.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.databinding.FragmentEditorRequestBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.picker.AssetPickerBottomSheet
import io.capstone.ludendorff.features.request.Request
import io.capstone.ludendorff.features.request.RequestViewModel
import io.capstone.ludendorff.features.shared.BaseEditorFragment

@AndroidEntryPoint
class RequestEditorFragment: BaseEditorFragment(), FragmentResultListener {
    private var _binding: FragmentEditorRequestBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val editorViewModel: RequestEditorViewModel by viewModels()
    private val viewModel: RequestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = buildContainerTransform()
        sharedElementReturnTransition = buildContainerTransform()

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
        _binding = FragmentEditorRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.toolbar)

        binding.root.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.toolbarActionButton.setText(R.string.button_send)
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_request_create,
            onNavigationClicked = { controller?.navigateUp() },
            customTitleView = binding.appBar.toolbarTitleTextView
        )

        registerForFragmentResult(arrayOf(AssetPickerBottomSheet.REQUEST_KEY_PICK), this)
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
    }

    override fun onResume() {
        super.onResume()

        binding.assetTextInputLayout.setEndIconOnClickListener {
            if (editorViewModel.asset != null) {
                editorViewModel.asset = null

                binding.detailsLayout.hide()
                binding.assetTextInput.setText(R.string.hint_not_set)
                binding.categoryTextView.text = null
                binding.assetStatusTextView.text = null

                binding.assetTextInputLayout.setEndIconDrawable(R.drawable.ic_hero_chevron_down)
            } else
                AssetPickerBottomSheet(childFragmentManager)
                    .show()
        }

        binding.appBar.toolbarActionButton.setOnClickListener {
            if (editorViewModel.asset == null) {
                createSnackbar(R.string.feedback_empty_asset, view = binding.snackbarAnchor)
                return@setOnClickListener
            }

            val request = Request()
            request.asset = editorViewModel.asset?.minimize()
            request.submittedTimestamp = Timestamp.now()
            request.petitioner = editorViewModel.user

            viewModel.create(request)
            controller?.navigateUp()
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            AssetPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Asset>(AssetPickerBottomSheet.EXTRA_ASSET)?.let {
                    editorViewModel.asset = it

                    binding.detailsLayout.isVisible = true
                    binding.assetTextInput.setText(it.assetName)
                    binding.categoryTextView.text = it.category?.categoryName
                    binding.assetTextInputLayout.setEndIconDrawable(R.drawable.ic_hero_x)

                    it.status?.getStringRes()?.also { res ->
                        binding.assetStatusTextView.setText(res)
                    }
                }
            }
        }
    }

}