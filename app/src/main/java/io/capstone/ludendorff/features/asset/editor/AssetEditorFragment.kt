package io.capstone.ludendorff.features.asset.editor

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.databinding.FragmentEditorAssetBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.AssetViewModel
import io.capstone.ludendorff.features.asset.qrcode.QRCodeViewBottomSheet
import io.capstone.ludendorff.features.type.Type
import io.capstone.ludendorff.features.type.picker.TypePickerBottomSheet
import io.capstone.ludendorff.features.shared.BaseEditorFragment
import io.capstone.ludendorff.features.shared.BaseFragment

@AndroidEntryPoint
class AssetEditorFragment: BaseEditorFragment(), FragmentResultListener,
    BaseFragment.CascadeMenuDelegate {
    private var _binding: FragmentEditorAssetBinding? = null
    private var controller: NavController? = null
    private var requestKey = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val editorViewModel: AssetEditorViewModel by viewModels()
    private val viewModel: AssetViewModel by activityViewModels()

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
        _binding = FragmentEditorAssetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.toolbar, arrayOf(binding.remarksTextInputLayout))

        binding.root.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_asset_create,
            iconRes = R.drawable.ic_round_close_24,
            onNavigationClicked = { controller?.navigateUp() },
            menuRes = R.menu.menu_editor_asset,
            onMenuOptionClicked = ::onMenuItemClicked,
            customTitleView = binding.appBar.toolbarTitleTextView
        )

        arguments?.getParcelable<Asset>(EXTRA_ASSET)?.let {
            requestKey = REQUEST_KEY_UPDATE
            editorViewModel.asset = it

            binding.root.transitionName = TRANSITION_NAME_ROOT + it.stockNumber
            binding.appBar.toolbarTitleTextView.setText(R.string.title_asset_update)
            binding.appBar.toolbar.menu.findItem(R.id.action_remove).isVisible = true

            binding.stockNumberTextInput.setText(it.stockNumber)
            binding.descriptionTextInput.setText(it.description)
            binding.classificationTextInput.setText(it.classification)
            binding.typeTextInput.setText(it.type?.typeName)
            binding.unitOfMeasureTextInput.setText(it.unitOfMeasure)
            binding.unitValueTextInput.setText(it.unitValue.toString())
            binding.remarksTextInput.setText(it.remarks)

            if (it.type != null)
                binding.typeTextInputLayout.setEndIconDrawable(R.drawable.ic_round_close_24)
        }

        registerForFragmentResult(
            arrayOf(TypePickerBottomSheet.REQUEST_KEY_PICK), this)
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
    }

    override fun onResume() {
        super.onResume()

        binding.typeTextInputLayout.setEndIconOnClickListener {
            if (editorViewModel.asset.type != null) {
                editorViewModel.asset.type = null
                binding.typeTextInput.setText(R.string.hint_not_set)
                binding.typeTextInputLayout.setEndIconDrawable(R.drawable.ic_round_keyboard_arrow_down_24)
            } else
                TypePickerBottomSheet(childFragmentManager)
                    .show()
        }
        binding.appBar.toolbarActionButton.setOnClickListener {
            editorViewModel.asset.stockNumber = binding.stockNumberTextInput.text.toString()
            editorViewModel.asset.description = binding.descriptionTextInput.text.toString()
            editorViewModel.asset.classification = binding.classificationTextInput.text.toString()
            editorViewModel.asset.unitOfMeasure = binding.unitOfMeasureTextInput.text.toString()
            editorViewModel.asset.unitValue = binding.unitValueTextInput.text.toString().toDoubleOrNull() ?: 0.0
            editorViewModel.asset.remarks = binding.remarksTextInput.text.toString()

            if (editorViewModel.asset.description.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_asset_description, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            if (editorViewModel.asset.classification.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_asset_classification, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            if (editorViewModel.asset.unitOfMeasure.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_unit_of_measure, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            if (editorViewModel.asset.unitValue <= 0) {
                createSnackbar(R.string.feedback_empty_unit_value, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            if (editorViewModel.asset.type == null) {
                if (editorViewModel.previousType == null) {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.dialog_no_type_title)
                        message(R.string.dialog_no_type_message)
                        positiveButton(android.R.string.ok)
                    }
                } else {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.dialog_no_type_title)
                        message(R.string.dialog_no_type_message_has_previous)
                        positiveButton(R.string.button_continue) {
                            onSaveAsset()
                        }
                        negativeButton(R.string.button_cancel)
                    }
                }
            } else {
                onSaveAsset()
            }
        }
    }

    private fun onSaveAsset() {
        if (requestKey == REQUEST_KEY_CREATE)
            viewModel.create(editorViewModel.asset)
        else viewModel.update(editorViewModel.asset, editorViewModel.previousType)
        controller?.navigateUp()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboardFromCurrentFocus(binding.root)
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            TypePickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Type>(TypePickerBottomSheet.EXTRA_CATEGORY)?.let {
                    binding.typeTextInput.setText(it.typeName)
                    binding.typeTextInputLayout.setEndIconDrawable(R.drawable.ic_round_close_24)

                    if (editorViewModel.previousType?.typeId != it.typeId)
                        editorViewModel.triggerCategoryChanged(it)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun onMenuItemClicked(id: Int) {
        when(id) {
            R.id.action_view_qrcode -> {
                QRCodeViewBottomSheet(childFragmentManager).show {
                    arguments = bundleOf(
                        QRCodeViewBottomSheet.EXTRA_ASSET_ID to editorViewModel.asset.stockNumber
                    )
                }
            }
            R.id.action_remove -> {
                if (requestKey == REQUEST_KEY_UPDATE) {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.dialog_remove_asset_title)
                        message(R.string.dialog_remove_asset_message)
                        positiveButton(R.string.button_remove) {
                            viewModel.remove(editorViewModel.asset)
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
        const val EXTRA_ASSET = "extra:asset"
    }

}