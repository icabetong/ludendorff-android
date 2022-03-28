package io.capstone.ludendorff.features.inventory.editor

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
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.MonthPickerDialog
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.toLocalDate
import io.capstone.ludendorff.components.extensions.toTimestamp
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.utils.DateTimeFormatter.Companion.getMonthStringRes
import io.capstone.ludendorff.components.utils.DateTimeFormatter.Companion.getDateFormatter
import io.capstone.ludendorff.databinding.FragmentEditorInventoryReportBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.picker.AssetPickerBottomSheet
import io.capstone.ludendorff.features.inventory.InventoryReport
import io.capstone.ludendorff.features.inventory.InventoryReportViewModel
import io.capstone.ludendorff.features.inventory.item.InventoryItem
import io.capstone.ludendorff.features.inventory.item.InventoryItemAdapter
import io.capstone.ludendorff.features.inventory.item.InventoryItemEditorBottomSheet
import io.capstone.ludendorff.features.shared.BaseEditorFragment
import io.capstone.ludendorff.features.shared.BaseFragment
import java.util.*

@AndroidEntryPoint
class InventoryReportEditorFragment: BaseEditorFragment(), FragmentResultListener,
    OnItemActionListener<InventoryItem>, BaseFragment.CascadeMenuDelegate {
    private var _binding: FragmentEditorInventoryReportBinding? = null
    private var controller: NavController? = null
    private var requestKey: String = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val inventoryAdapter = InventoryItemAdapter(this)
    private val editorViewModel: InventoryReportEditorViewModel by viewModels()
    private val viewModel: InventoryReportViewModel by activityViewModels()
    private val formatter = getDateFormatter(isShort = true, withYear = true)

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
        _binding = FragmentEditorInventoryReportBinding.inflate(inflater, container, false)
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
            titleRes = R.string.title_inventory_create,
            iconRes = R.drawable.ic_round_close_24,
            menuRes = R.menu.menu_editor,
            onMenuOptionClicked = ::onMenuItemClicked,
            onNavigationClicked = { controller?.navigateUp() },
            customTitleView = binding.appBar.toolbarTitleTextView
        )

        arguments?.getParcelable<InventoryReport>(EXTRA_INVENTORY_REPORT)?.let {
            requestKey = REQUEST_KEY_UPDATE
            editorViewModel.inventoryReport = it

            binding.root.transitionName = TRANSITION_NAME_ROOT + it.inventoryReportId
            binding.appBar.toolbarTitleTextView.setText(R.string.title_inventory_update)
            binding.appBar.toolbar.menu.findItem(R.id.action_remove).isVisible = true

            binding.fundClusterTextInput.setText(it.fundCluster)
            binding.entityNameTextInput.setText(it.entityName)
            binding.entityPositionTextInput.setText(it.entityPosition)
            binding.yearMonthTextInput.setText(it.yearMonth)
            binding.accountabilityDateTextInput.setText(
                formatter.format(it.accountabilityDate.toLocalDate())
            )
        }

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = inventoryAdapter
        }

        registerForFragmentResult(arrayOf(
            AssetPickerBottomSheet.REQUEST_KEY_PICK,
            InventoryItemEditorBottomSheet.REQUEST_KEY_CREATE,
            InventoryItemEditorBottomSheet.REQUEST_KEY_UPDATE), this)
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        editorViewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressIndicator.isVisible = it
            binding.addActionButton.addActionButton.isEnabled = !it
        }
        editorViewModel.inventoryItems.observe(viewLifecycleOwner) {
            inventoryAdapter.submit(it)
        }
    }

    override fun onStop() {
        super.onStop()
        hideKeyboardFromCurrentFocus(binding.root)
    }

    override fun onResume() {
        super.onResume()

        binding.addActionButton.addActionButton.setOnClickListener {
            AssetPickerBottomSheet(childFragmentManager)
                .show()
        }
        binding.appBar.toolbarActionButton.setOnClickListener {
            with(editorViewModel.inventoryReport) {
                fundCluster = binding.fundClusterTextInput.text.toString()
                entityName = binding.entityNameTextInput.text.toString()
                entityPosition = binding.entityPositionTextInput.text.toString()
                items = editorViewModel.items
            }

            if (editorViewModel.inventoryReport.fundCluster.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_fund_cluster, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            if (editorViewModel.inventoryReport.entityName.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_entity_name, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            if (editorViewModel.inventoryReport.entityPosition.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_entity_position, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            if (editorViewModel.inventoryReport.items.isEmpty()) {
                createSnackbar(R.string.feedback_empty_asset_items, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            onSaveInventoryReport()
        }

        binding.yearMonthTextInputLayout.setEndIconOnClickListener(::invokeYearMonthPicker)
        binding.yearMonthTextInput.setOnClickListener(::invokeYearMonthPicker)
        binding.accountabilityDateTextInputLayout.setEndIconOnClickListener(::invokeDateTimePicker)
        binding.accountabilityDateTextInput.setOnClickListener(::invokeDateTimePicker)
    }

    private fun onSaveInventoryReport() {
        if (requestKey == REQUEST_KEY_CREATE)
            viewModel.create(editorViewModel.inventoryReport)
        else viewModel.update(editorViewModel.inventoryReport)
        controller?.navigateUp()
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            AssetPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Asset>(AssetPickerBottomSheet.EXTRA_ASSET)?.let {
                    InventoryItemEditorBottomSheet(childFragmentManager).show {
                        arguments = bundleOf(
                            InventoryItemEditorBottomSheet.EXTRA_ASSET to it
                        )
                    }
                }
            }
            InventoryItemEditorBottomSheet.REQUEST_KEY_CREATE -> {
                result.getParcelable<InventoryItem>(
                    InventoryItemEditorBottomSheet.EXTRA_INVENTORY_ITEM)?.let {
                    editorViewModel.insert(it)
                }
            }
            InventoryItemEditorBottomSheet.REQUEST_KEY_UPDATE -> {
                result.getParcelable<InventoryItem>(
                    InventoryItemEditorBottomSheet.EXTRA_INVENTORY_ITEM)?.let {
                    editorViewModel.update(it)
                }
            }
        }
    }

    private fun invokeDateTimePicker(view: View) {
        MaterialDialog(requireContext()).show {
            lifecycleOwner(viewLifecycleOwner)
            datePicker { _, calendar ->
                editorViewModel.inventoryReport.accountabilityDate = calendar.toTimestamp()

                binding.accountabilityDateTextInput.setText(formatter.format(calendar.toLocalDate()))
            }
        }
    }

    private fun invokeYearMonthPicker(view: View) {
        val today = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        MonthPickerDialog.Builder(
            requireContext(), { month, year ->
                val yearMonth = "${getString(getMonthStringRes(month))} $year"
                binding.yearMonthTextInput.setText(yearMonth)
                editorViewModel.inventoryReport.yearMonth = yearMonth
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH)
        ).build().show()
    }

    override fun onActionPerformed(
        data: InventoryItem?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        when(action) {
            OnItemActionListener.Action.SELECT -> {
                InventoryItemEditorBottomSheet(childFragmentManager).show {
                    arguments = bundleOf(InventoryItemEditorBottomSheet.EXTRA_INVENTORY_ITEM to data)
                }
            }
            OnItemActionListener.Action.DELETE -> {}
        }
    }

    override fun onMenuItemClicked(id: Int) {
        when(id) {
            R.id.action_remove -> {
                if (requestKey == REQUEST_KEY_UPDATE) {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.dialog_remove_report_title)
                        message(R.string.dialog_remove_report_message)
                        positiveButton(R.string.button_remove) {
                            viewModel.remove(editorViewModel.inventoryReport)
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
        const val EXTRA_INVENTORY_REPORT = "extra:inventory"
    }
}