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
import androidx.lifecycle.lifecycleScope
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
import io.capstone.ludendorff.features.core.entity.Entity
import io.capstone.ludendorff.features.core.entity.EntityViewModel
import io.capstone.ludendorff.features.inventory.picker.InventoryReportPickerFragment
import io.capstone.ludendorff.features.issued.item.picker.GroupedIssuedItem
import io.capstone.ludendorff.features.issued.item.picker.IssuedItemPickerFragment
import io.capstone.ludendorff.features.shared.BaseEditorFragment
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.stockcard.StockCard
import io.capstone.ludendorff.features.stockcard.StockCardViewModel
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntry
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntryAdapter
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntryEditorBottomSheet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StockCardEditorFragment: BaseEditorFragment(), BaseFragment.CascadeMenuDelegate,
    OnItemActionListener<StockCardEntry>, FragmentResultListener {
    private var _binding: FragmentEditorStockCardBinding? = null
    private var controller: NavController? = null
    private var requestKey: String = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val entryAdapter = StockCardEntryAdapter(this)
    private val editorViewModel: StockCardEditorViewModel by activityViewModels()
    private val entityViewModel: EntityViewModel by activityViewModels()
    private val viewModel: StockCardViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = buildContainerTransform()
        sharedElementReturnTransition = buildContainerTransform()

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    editorViewModel.clear()
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
        setInsets(view, binding.appBar.toolbar, arrayOf(binding.recyclerView))

        binding.root.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_stock_card_create,
            iconRes = R.drawable.ic_round_close_24,
            menuRes = R.menu.menu_editor,
            customTitleView = binding.appBar.toolbarTitleTextView,
            onMenuOptionClicked = ::onMenuItemClicked,
            onNavigationClicked = {
                editorViewModel.clear()
                controller?.navigateUp()
            },
        )

        arguments?.getParcelable<StockCard>(EXTRA_STOCK_CARD)?.let {
            requestKey = REQUEST_KEY_UPDATE
            editorViewModel.stockCard = it

            binding.root.transitionName = TRANSITION_NAME_ROOT + it.stockCardId
            binding.appBar.toolbarTitleTextView.setText(R.string.title_stock_card_update)
            binding.appBar.toolbar.menu.findItem(R.id.action_remove).isVisible = true

            binding.assetTextInput.setText(it.description)
        }

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = entryAdapter
        }

        registerForFragmentResult(arrayOf(
            IssuedItemPickerFragment.REQUEST_KEY_PICK,
            StockCardEntryEditorBottomSheet.REQUEST_KEY_CREATE,
            StockCardEntryEditorBottomSheet.REQUEST_KEY_UPDATE), this)
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        editorViewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressIndicator.isVisible = it
        }
        editorViewModel.entries.observe(viewLifecycleOwner) {
            entryAdapter.submit(it)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.assetTextInputLayout.setEndIconOnClickListener {
            hideKeyboardFromCurrentFocus(binding.root)
            IssuedItemPickerFragment(childFragmentManager)
                .show()
        }
        binding.appBar.toolbarActionButton.setOnClickListener {
            editorViewModel.stockCard.balances = editorViewModel.balances
            editorViewModel.stockCard.entries = editorViewModel.items

            val hasConfiguredEntries = editorViewModel.stockCard.entries.all { e ->
                editorViewModel.balances.values.any { it.containsEntryId(e.stockCardEntryId) }
            }

            if (editorViewModel.stockCard.stockNumber.isBlank()) {
                createSnackbar(R.string.feedback_empty_asset, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            if (!hasConfiguredEntries) {
                createSnackbar(R.string.feedback_empty_entries, view = binding.snackbarAnchor)
                return@setOnClickListener
            }

            onSaveAsset()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            entityViewModel.entity.collectLatest {
                editorViewModel.stockCard.entityName = it?.entityName
            }
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
        when(action) {
            OnItemActionListener.Action.SELECT -> {
                StockCardEntryEditorBottomSheet(childFragmentManager).show {
                    arguments = bundleOf(StockCardEntryEditorBottomSheet.EXTRA_STOCK_CARD_ENTRY
                            to data)
                }
            }
            OnItemActionListener.Action.DELETE -> {
                InventoryReportPickerFragment(childFragmentManager).show {
                    data?.inventoryReportSourceId = it?.inventoryReportId
                    if (data != null) {
                        editorViewModel.modifyBalances(data)
                    }
                }
            }
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            IssuedItemPickerFragment.REQUEST_KEY_PICK -> {
                result.getParcelable<GroupedIssuedItem>(IssuedItemPickerFragment.EXTRA_ISSUED_ITEM)?.let {
                    editorViewModel.stockCard.stockNumber = it.stockNumber
                    editorViewModel.setEntries(it.items.map { item -> item.toStockCardEntry(it.reference) })
                    if (it.items.isNotEmpty()) {
                        val sample = it.items[0]
                        editorViewModel.stockCard.description = sample.description
                        editorViewModel.stockCard.unitOfMeasure = sample.unitOfMeasure
                        editorViewModel.stockCard.unitPrice = sample.unitCost

                        binding.assetTextInput.setText(sample.description)
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