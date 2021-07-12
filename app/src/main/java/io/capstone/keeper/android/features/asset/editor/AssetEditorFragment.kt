package io.capstone.keeper.android.features.asset.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.FragmentEditorAssetBinding
import io.capstone.keeper.android.features.asset.qrcode.QRCodeViewBottomSheet
import io.capstone.keeper.android.features.specs.SpecsEditorBottomSheet
import io.capstone.keeper.android.features.shared.components.BaseEditorFragment
import io.capstone.keeper.android.features.specs.SpecsAdapter

class AssetEditorFragment: BaseEditorFragment(), FragmentResultListener {
    private var _binding: FragmentEditorAssetBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: AssetEditorViewModel by viewModels()
    private val specificationAdapter = SpecsAdapter()

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

        binding.recyclerView.adapter = specificationAdapter

        registerForFragmentResult(arrayOf(SpecsEditorBottomSheet.REQUEST_KEY_CREATE,
            SpecsEditorBottomSheet.REQUEST_KEY_UPDATE), this)
    }

    override fun onStart() {
        super.onStart()

        binding.addAction.addActionButton.setOnClickListener {
            SpecsEditorBottomSheet(childFragmentManager).show()
        }
    }

    override fun onResume() {
        super.onResume()


    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            SpecsEditorBottomSheet.REQUEST_KEY_CREATE -> {
                result.getSerializable(SpecsEditorBottomSheet.EXTRA_SPECIFICATION)?.let {
                    if (it is Pair<*, *>) {
                        viewModel.specifications[it.first as String] = it.second as String
                        specificationAdapter.submitList(viewModel.specifications.toList())
                    }
                }
            }
        }
    }

}