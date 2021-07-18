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
import io.capstone.keeper.R
import io.capstone.keeper.components.extensions.setup
import io.capstone.keeper.components.interfaces.OnItemActionListener
import io.capstone.keeper.databinding.FragmentEditorAssetBinding
import io.capstone.keeper.features.asset.qrcode.QRCodeViewBottomSheet
import io.capstone.keeper.features.category.Category
import io.capstone.keeper.features.category.picker.CategoryPickerBottomSheet
import io.capstone.keeper.features.specs.SpecsEditorBottomSheet
import io.capstone.keeper.features.shared.components.BaseEditorFragment
import io.capstone.keeper.features.specs.SpecsAdapter

class AssetEditorFragment: BaseEditorFragment(), FragmentResultListener,
    OnItemActionListener<Pair<String, String>> {
    private var _binding: FragmentEditorAssetBinding? = null
    private var controller: NavController? = null

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

        controller = Navigation.findNavController(view)
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_asset_create,
            iconRes = R.drawable.ic_hero_x,
            onNavigationClicked = { controller?.navigateUp() },
            menuRes = R.menu.menu_editor_asset,
            onMenuOptionClicked = {
                when(it) {
                    R.id.action_view_qrcode ->
                        QRCodeViewBottomSheet(childFragmentManager).show {
                            arguments = bundleOf(
                                QRCodeViewBottomSheet.EXTRA_ASSET_ID to viewModel.asset.assetId
                            )
                        }
                }
            }
        )

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

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
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
            CategoryPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Category>(CategoryPickerBottomSheet.EXTRA_CATEGORY)?.let {
                    binding.categoryTextView.text = it.categoryName
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
                data?.let {
                    viewModel.removeSpecification(it)
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