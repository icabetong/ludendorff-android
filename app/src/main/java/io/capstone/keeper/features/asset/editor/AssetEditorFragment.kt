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
import io.capstone.keeper.databinding.FragmentEditorAssetBinding
import io.capstone.keeper.features.asset.qrcode.QRCodeViewBottomSheet
import io.capstone.keeper.features.category.Category
import io.capstone.keeper.features.category.picker.CategoryPickerBottomSheet
import io.capstone.keeper.features.specs.SpecsEditorBottomSheet
import io.capstone.keeper.features.shared.components.BaseEditorFragment

class AssetEditorFragment: BaseEditorFragment(), FragmentResultListener {
    private var _binding: FragmentEditorAssetBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: AssetEditorViewModel by viewModels()

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
        setupToolbar(binding.appBar.toolbar, {
            controller?.navigateUp()
        },  icon = R.drawable.ic_hero_x,
            menu = R.menu.menu_editor_asset,
            menuListener = { id ->
                if (id == R.id.action_view_qrcode) {
                    QRCodeViewBottomSheet(childFragmentManager).show {
                        arguments = bundleOf(
                            QRCodeViewBottomSheet.EXTRA_ASSET_ID to viewModel.asset.assetId
                        )
                    }
                }
            })

        registerForFragmentResult(
            arrayOf(
                SpecsEditorBottomSheet.REQUEST_KEY_CREATE,
                SpecsEditorBottomSheet.REQUEST_KEY_UPDATE,
                CategoryPickerBottomSheet.REQUEST_KEY_PICK
            ), this)
    }

    override fun onStart() {
        super.onStart()

        binding.addAction.addActionButton.setOnClickListener {
            SpecsEditorBottomSheet(childFragmentManager).show()
        }
        binding.categoryTextInput.setOnClickListener {
            CategoryPickerBottomSheet(childFragmentManager).show()
        }
    }

    override fun onResume() {
        super.onResume()


    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            SpecsEditorBottomSheet.REQUEST_KEY_CREATE -> {

            }
            SpecsEditorBottomSheet.REQUEST_KEY_UPDATE -> {

            }
            CategoryPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Category>(CategoryPickerBottomSheet.EXTRA_CATEGORY)?.let {
                    binding.categoryTextInput.setText(it.categoryName)
                }
            }
        }
    }

}