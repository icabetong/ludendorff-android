package io.capstone.keeper.features.asset.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.components.extensions.setup
import io.capstone.keeper.components.interfaces.OnItemActionListener
import io.capstone.keeper.databinding.FragmentEditorAssetBinding
import io.capstone.keeper.features.asset.Asset
import io.capstone.keeper.features.asset.qrcode.QRCodeViewBottomSheet
import io.capstone.keeper.features.category.Category
import io.capstone.keeper.features.category.picker.CategoryPickerBottomSheet
import io.capstone.keeper.features.shared.components.BaseEditorFragment
import io.capstone.keeper.features.specs.SpecsAdapter
import io.capstone.keeper.features.specs.editor.SpecsEditorBottomSheet

@AndroidEntryPoint
class AssetEditorFragment: BaseEditorFragment(), FragmentResultListener,
    OnItemActionListener<Pair<String, String>> {
    private var _binding: FragmentEditorAssetBinding? = null
    private var controller: NavController? = null
    private var requestKey = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val viewModel: AssetEditorViewModel by viewModels()
    private val specsAdapter = SpecsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = buildContainerTransform()
        sharedElementReturnTransition = buildContainerTransform()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorAssetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.transitionName = TRANSITION_NAME_ROOT

        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_asset_create,
            iconRes = R.drawable.ic_hero_x,
            onNavigationClicked = { controller?.navigateUp() },
            menuRes = R.menu.menu_editor_asset,
            onMenuOptionClicked = {
                when(it) {
                    R.id.action_view_qrcode -> {
                        QRCodeViewBottomSheet(childFragmentManager).show {
                            arguments = bundleOf(
                                QRCodeViewBottomSheet.EXTRA_ASSET_ID to viewModel.asset.assetId
                            )
                        }
                    }
                    R.id.action_remove -> {
                        viewModel.remove()
                        controller?.navigateUp()
                    }
                }
            }
        )

        arguments?.getParcelable<Asset>(EXTRA_ASSET)?.let {
            requestKey = REQUEST_KEY_UPDATE
            viewModel.asset = it
            viewModel.setSpecifications(ArrayList(it.specifications.toList()))

            binding.root.transitionName = TRANSITION_NAME_ROOT + it.assetId
            binding.appBar.toolbar.setTitle(R.string.title_asset_update)

            binding.assetNameTextInput.setText(it.assetName)
            binding.categoryTextView.text = it.category?.categoryName
            when(it.status) {
                Asset.Status.OPERATIONAL -> binding.operationalChip.isChecked = true
                Asset.Status.IDLE -> binding.idleChip.isChecked = true
                Asset.Status.UNDER_MAINTENANCE -> binding.underMaintenanceChip.isChecked = true
                Asset.Status.RETIRED -> binding.retiredChip.isChecked = true
                null -> binding.idleChip.isChecked = true
            }
        }

        with(binding.recyclerView) {
            adapter = specsAdapter
        }

        registerForFragmentResult(
            arrayOf(
                SpecsEditorBottomSheet.REQUEST_KEY_CREATE,
                SpecsEditorBottomSheet.REQUEST_KEY_UPDATE,
                CategoryPickerBottomSheet.REQUEST_KEY_PICK
            ), this)
    }

    override fun onStart() {
        super.onStart()

        viewModel.specifications.observe(viewLifecycleOwner) {
            specsAdapter.submitList(it)
        }

        binding.addAction.addActionButton.setOnClickListener {
            SpecsEditorBottomSheet(childFragmentManager).show()
        }
        binding.categoryTextView.setOnClickListener {
            CategoryPickerBottomSheet(childFragmentManager).show()
        }
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            viewModel.asset.assetName = binding.assetNameTextInput.text.toString()

            if (viewModel.asset.assetName.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_asset_name)
                return@setOnClickListener
            }
            if (viewModel.getSpecifications().isNullOrEmpty()) {
                MaterialDialog(requireContext()).show {
                    lifecycleOwner(viewLifecycleOwner)
                    title(R.string.dialog_save_without_specification_title)
                    message(R.string.dialog_save_without_specification_message)
                    positiveButton(R.string.button_continue)
                    negativeButton { return@negativeButton }
                }
            }

            if (requestKey == REQUEST_KEY_CREATE)
                viewModel.insert()
            else viewModel.update()
            controller?.navigateUp()
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            CategoryPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Category>(CategoryPickerBottomSheet.EXTRA_CATEGORY)?.let {
                    binding.categoryTextView.text = it.categoryName

                    if (viewModel.previousCategoryId != it.categoryId)
                        viewModel.triggerCategoryChanged(it)
                }
            }
            SpecsEditorBottomSheet.REQUEST_KEY_CREATE -> {
                val key = result.getString(SpecsEditorBottomSheet.EXTRA_KEY)
                val value = result.getString(SpecsEditorBottomSheet.EXTRA_VALUE)
                if (!key.isNullOrBlank() && !value.isNullOrBlank()) {
                    val specification = Pair(key, value)
                    if (viewModel.checkSpecificationIfExists(specification))
                        createSnackbar(R.string.feedback_specification_exists)
                    else viewModel.addSpecification(specification)
                }
            }
            SpecsEditorBottomSheet.REQUEST_KEY_UPDATE -> {
                val key = result.getString(SpecsEditorBottomSheet.EXTRA_KEY)
                val value = result.getString(SpecsEditorBottomSheet.EXTRA_VALUE)
                if (!key.isNullOrBlank() && !value.isNullOrBlank()) {
                    viewModel.updateSpecification(Pair(key, value))
                }
            }
        }
    }

    override fun onActionPerformed(
        data: Pair<String, String>?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        when (action) {
            OnItemActionListener.Action.SELECT -> {
                SpecsEditorBottomSheet(childFragmentManager).show {
                    arguments = bundleOf(
                        SpecsEditorBottomSheet.EXTRA_KEY to data?.first,
                        SpecsEditorBottomSheet.EXTRA_VALUE to data?.second
                    )
                }
            }
            OnItemActionListener.Action.DELETE -> {
                MaterialDialog(requireContext()).show {
                    lifecycleOwner(viewLifecycleOwner)
                    title(R.string.dialog_remove_specification_title)
                    message(R.string.dialog_remove_specification_message)
                    positiveButton(R.string.button_remove) {
                        data?.let {
                            viewModel.removeSpecification(it)
                            createSnackbar(R.string.feedback_specification_removed)
                        }
                    }
                    negativeButton(R.string.button_cancel)
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