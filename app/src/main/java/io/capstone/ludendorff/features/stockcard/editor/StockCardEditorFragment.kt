package io.capstone.ludendorff.features.stockcard.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentEditorStockCardBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.picker.AssetPickerBottomSheet
import io.capstone.ludendorff.features.shared.BaseEditorFragment
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.stockcard.StockCard
import io.capstone.ludendorff.features.stockcard.StockCardViewModel
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntry
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntryAdapter
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntryEditorBottomSheet

@AndroidEntryPoint
class StockCardEditorFragment: BaseEditorFragment(), BaseFragment.CascadeMenuDelegate,
    OnItemActionListener<StockCardEntry>, FragmentResultListener {
    private var _binding: FragmentEditorStockCardBinding? = null
    private var controller: NavController? = null
    private var requestKey: String = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val entryAdapter = StockCardEntryAdapter(this)
    private val editorViewModel: StockCardEditorViewModel by viewModels()
    private val viewModel: StockCardViewModel by activityViewModels()

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
        _binding = FragmentEditorStockCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.toolbar, arrayOf(binding.addActionButton.root))

        binding.root.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_stock_card_create,
            iconRes = R.drawable.ic_round_close_24,
            menuRes = R.menu.menu_editor,
            onMenuOptionClicked = ::onMenuItemClicked,
            onNavigationClicked = { controller?.navigateUp() },
            customTitleView = binding.appBar.toolbarTitleTextView
        )

        arguments?.getParcelable<StockCard>(EXTRA_STOCK_CARD)?.let {
            requestKey = REQUEST_KEY_UPDATE
            editorViewModel.stockCard = it

            binding.root.transitionName = TRANSITION_NAME_ROOT + it.stockCardId
            binding.appBar.toolbarTitleTextView.setText(R.string.title_stock_card_update)
            binding.appBar.toolbar.menu.findItem(R.id.action_remove).isVisible = true

            binding.entityNameTextInput.setText(it.entityName)
            binding.assetTextInput.setText(it.description)
        }

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = entryAdapter
        }

        registerForFragmentResult(arrayOf(AssetPickerBottomSheet.REQUEST_KEY_PICK,
            StockCardEntryEditorBottomSheet.REQUEST_KEY_CREATE,
            StockCardEntryEditorBottomSheet.REQUEST_KEY_UPDATE), this)
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        editorViewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressIndicator.isVisible = it
            binding.addActionButton.addActionButton.isEnabled = !it
        }
        editorViewModel.entries.observe(viewLifecycleOwner) {
            entryAdapter.submit(it)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.assetTextInputLayout.setEndIconOnClickListener {
            AssetPickerBottomSheet(childFragmentManager)
                .show()
        }
        binding.addActionButton.addActionButton.setOnClickListener {
            StockCardEntryEditorBottomSheet(childFragmentManager)
                .show()
        }
        binding.appBar.toolbarActionButton.setOnClickListener {
            editorViewModel.stockCard.entityName = binding.entityNameTextInput.text.toString()
            editorViewModel.stockCard.entries = editorViewModel.items

            if (editorViewModel.stockCard.entityName.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_entity_name, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            if (editorViewModel.stockCard.stockNumber.isBlank()) {
                createSnackbar(R.string.feedback_empty_asset, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            if (editorViewModel.stockCard.entries.isEmpty()) {
                createSnackbar(R.string.feedback_empty_entries, view = binding.snackbarAnchor)
                return@setOnClickListener
            }

            onSaveAsset()
        }
    }

    private fun onSaveAsset() {
        if (requestKey == REQUEST_KEY_CREATE)
            viewModel.create(editorViewModel.stockCard)
        else viewModel.update(editorViewModel.stockCard)
        controller?.navigateUp()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboardFromCurrentFocus(binding.root)
    }

    override fun onMenuItemClicked(id: Int) {
        when(id) {
            R.id.action_remove -> {
                if (requestKey == REQUEST_KEY_UPDATE) {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.dialog_remove_stock_card_title)
                        message(R.string.dialog_remove_stock_card_message)
                        positiveButton(R.string.button_remove) {
                            viewModel.remove(editorViewModel.stockCard)
                            controller?.navigateUp()
                        }
                        negativeButton(R.string.button_cancel)
                    }
                }
            }
        }
    }

    override fun onActionPerformed(
        data: StockCardEntry?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        if (action == OnItemActionListener.Action.SELECT) {
            StockCardEntryEditorBottomSheet(childFragmentManager).show {
                arguments =
                    bundleOf(StockCardEntryEditorBottomSheet.EXTRA_STOCK_CARD_ENTRY to data)
            }
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            AssetPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Asset>(AssetPickerBottomSheet.EXTRA_ASSET)?.let {
                    binding.assetTextInput.setText(it.description)

                    with(editorViewModel.stockCard) {
                        stockNumber = it.stockNumber
                        description = it.description
                        unitPrice = it.unitValue
                        unitOfMeasure = it.unitOfMeasure
                    }
                }
            }
            StockCardEntryEditorBottomSheet.REQUEST_KEY_CREATE -> {
                result.getParcelable<StockCardEntry>(StockCardEntryEditorBottomSheet.EXTRA_STOCK_CARD_ENTRY)?.let {
                    editorViewModel.insert(it)
                }
            }
            StockCardEntryEditorBottomSheet.REQUEST_KEY_UPDATE -> {
                result.getParcelable<StockCardEntry>(StockCardEntryEditorBottomSheet.EXTRA_STOCK_CARD_ENTRY)?.let {
                    editorViewModel.update(it)
                }
            }
        }
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"
        const val EXTRA_STOCK_CARD = "extra:card"
    }
}