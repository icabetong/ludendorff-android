package io.capstone.ludendorff.features.issued.editor

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
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.toLocalDate
import io.capstone.ludendorff.components.extensions.toTimestamp
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.utils.DateTimeFormatter.Companion.getDateFormatter
import io.capstone.ludendorff.databinding.FragmentEditorIssuedReportBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.picker.AssetPickerBottomSheet
import io.capstone.ludendorff.features.issued.IssuedReport
import io.capstone.ludendorff.features.issued.IssuedReportViewModel
import io.capstone.ludendorff.features.issued.item.IssuedItem
import io.capstone.ludendorff.features.issued.item.IssuedItemAdapter
import io.capstone.ludendorff.features.issued.item.IssuedItemEditorBottomSheet
import io.capstone.ludendorff.features.shared.BaseEditorFragment
import io.capstone.ludendorff.features.shared.BaseFragment

@AndroidEntryPoint
class IssuedReportEditorFragment: BaseEditorFragment(), FragmentResultListener,
    OnItemActionListener<IssuedItem>, BaseFragment.CascadeMenuDelegate {
    private var _binding: FragmentEditorIssuedReportBinding? = null
    private var controller: NavController? = null
    private var requestKey: String = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val editorViewModel: IssuedReportEditorViewModel by viewModels()
    private val viewModel: IssuedReportViewModel by activityViewModels()
    private val issuedItemAdapter = IssuedItemAdapter(this)
    private val formatter = getDateFormatter(withYear = true, isShort = true)

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
        _binding = FragmentEditorIssuedReportBinding.inflate(inflater, container, false)
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
            titleRes = R.string.title_issued_create,
            iconRes = R.drawable.ic_round_close_24,
            menuRes = R.menu.menu_editor,
            onMenuOptionClicked = ::onMenuItemClicked,
            onNavigationClicked = { controller?.navigateUp() },
            customTitleView = binding.appBar.toolbarTitleTextView
        )

        arguments?.getParcelable<IssuedReport>(EXTRA_ISSUED_REPORT)?.let {
            requestKey = REQUEST_KEY_UPDATE
            editorViewModel.issuedReport = it

            binding.root.transitionName = TRANSITION_NAME_ROOT + it.issuedReportId
            binding.appBar.toolbarTitleTextView.setText(R.string.title_issued_update)
            binding.appBar.toolbar.menu.findItem(R.id.action_remove).isVisible = true

            binding.fundClusterTextInput.setText(it.fundCluster)
            binding.entityNameTextInput.setText(it.entityName)
            binding.serialNumberTextInput.setText(it.serialNumber)
            binding.dateTextInput.setText(formatter.format(it.date.toLocalDate()))
        }

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = issuedItemAdapter
        }

        registerForFragmentResult(arrayOf(
            AssetPickerBottomSheet.REQUEST_KEY_PICK,
            IssuedItemEditorBottomSheet.REQUEST_KEY_CREATE,
            IssuedItemEditorBottomSheet.REQUEST_KEY_UPDATE), this)
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        editorViewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressIndicator.isVisible = it
            binding.addActionButton.addActionButton.isEnabled = !it
        }
        editorViewModel.issuedItems.observe(viewLifecycleOwner) {
            issuedItemAdapter.submit(it)
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
            with(editorViewModel.issuedReport) {
                fundCluster = binding.fundClusterTextInput.text.toString()
                entityName = binding.entityNameTextInput.text.toString()
                serialNumber = binding.serialNumberTextInput.text.toString()
            }

            if (editorViewModel.issuedReport.fundCluster.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_fund_cluster, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            if (editorViewModel.issuedReport.entityName.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_entity_name, view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            if (editorViewModel.issuedReport.serialNumber.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_serial_number, view = binding.snackbarAnchor)
                return@setOnClickListener
            }

            onSaveIssuedReport()
        }
        binding.dateTextInput.setOnClickListener(::invokeDatePicker)
        binding.dateTextInputLayout.setEndIconOnClickListener(::invokeDatePicker)
    }

    private fun onSaveIssuedReport() {
        if (requestKey == REQUEST_KEY_CREATE)
            viewModel.create(editorViewModel.issuedReport)
        else viewModel.update(editorViewModel.issuedReport)
        controller?.navigateUp()
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            AssetPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Asset>(AssetPickerBottomSheet.EXTRA_ASSET)?.let {
                    IssuedItemEditorBottomSheet(childFragmentManager).show {
                        arguments = bundleOf(AssetPickerBottomSheet.EXTRA_ASSET to it)
                    }
                }
            }
            IssuedItemEditorBottomSheet.REQUEST_KEY_CREATE -> {
                result.getParcelable<IssuedItem>(IssuedItemEditorBottomSheet.EXTRA_ISSUED_ITEM)?.let {
                    editorViewModel.insert(it)
                }
            }
            IssuedItemEditorBottomSheet.REQUEST_KEY_UPDATE -> {
                result.getParcelable<IssuedItem>(IssuedItemEditorBottomSheet.EXTRA_ISSUED_ITEM)?.let {
                    editorViewModel.update(it)
                }
            }
        }
    }

    private fun invokeDatePicker(view: View) {
        MaterialDialog(requireContext()).show {
            lifecycleOwner(viewLifecycleOwner)
            datePicker { _, calendar ->
                editorViewModel.issuedReport.date = calendar.toTimestamp()

                binding.dateTextInput.setText(formatter.format(calendar.toLocalDate()))
            }
        }
    }

    override fun onActionPerformed(
        data: IssuedItem?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        when(action) {
            OnItemActionListener.Action.SELECT -> {
                IssuedItemEditorBottomSheet(childFragmentManager).show {
                    arguments = bundleOf(IssuedItemEditorBottomSheet.EXTRA_ISSUED_ITEM to data)
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
                            viewModel.remove(editorViewModel.issuedReport)
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
        const val EXTRA_ISSUED_REPORT = "extra:issued"
    }
}