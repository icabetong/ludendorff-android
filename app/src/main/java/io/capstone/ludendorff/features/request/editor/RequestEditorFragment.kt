package io.capstone.ludendorff.features.request.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentEditorRequestBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.picker.AssetPickerBottomSheet
import io.capstone.ludendorff.features.shared.components.BaseEditorFragment
import io.capstone.ludendorff.features.specs.SpecsReadOnlyAdapter

@AndroidEntryPoint
class RequestEditorFragment: BaseEditorFragment(), FragmentResultListener {
    private var _binding: FragmentEditorRequestBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val specificationAdapter = SpecsReadOnlyAdapter()
    private val editorViewModel: RequestEditorViewModel by viewModels()

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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.toolbar, arrayOf(binding.recyclerView))

        binding.root.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.toolbarActionButton.setText(R.string.button_request)
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_request_create,
            iconRes = R.drawable.ic_hero_x,
            onNavigationClicked = { controller?.navigateUp() },
            customTitleView = binding.appBar.toolbarTitleTextView
        )

        with(binding.recyclerView) {
            adapter = specificationAdapter
        }

        registerForFragmentResult(arrayOf(AssetPickerBottomSheet.REQUEST_KEY_PICK), this)
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
    }

    override fun onResume() {
        super.onResume()

        binding.assetTextInput.setOnClickListener {
            AssetPickerBottomSheet(childFragmentManager)
                .show()
        }
        binding.assetTextInputLayout.setEndIconOnClickListener {
            editorViewModel.asset = null

            specificationAdapter.submitList(emptyList())
            binding.assetTextInputLayout.endIconDrawable = null
            binding.assetTextInput.text = null
            binding.assetStatusTextView.text = null
            binding.categoryTextView.text = null
            binding.assetDetailsLayout.hide()
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            AssetPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Asset>(AssetPickerBottomSheet.EXTRA_ASSET)?.let {
                    editorViewModel.asset = it

                    binding.assetTextInputLayout.setEndIconDrawable(R.drawable.ic_hero_x)
                    binding.assetTextInput.setText(it.assetName)
                    binding.categoryTextView.text = it.category?.categoryName
                    it.status?.getStringRes()?.let { res ->
                        binding.assetStatusTextView.setText(res)
                    }
                    specificationAdapter.submitList(it.specifications.toList())
                    binding.assetDetailsLayout.show()
                }
            }
        }
    }
}