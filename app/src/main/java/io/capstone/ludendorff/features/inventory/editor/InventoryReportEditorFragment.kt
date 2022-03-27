package io.capstone.ludendorff.features.inventory.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.firebase.Timestamp
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.MonthPickerDialog
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.toLocalDate
import io.capstone.ludendorff.components.extensions.toTimestamp
import io.capstone.ludendorff.components.utils.DateTimeFormatter.Companion.getMonthStringRes
import io.capstone.ludendorff.components.utils.DateTimeFormatter.Companion.getDateFormatter
import io.capstone.ludendorff.databinding.FragmentEditorInventoryReportBinding
import io.capstone.ludendorff.features.asset.picker.AssetPickerBottomSheet
import io.capstone.ludendorff.features.inventory.item.InventoryItemAdapter
import io.capstone.ludendorff.features.shared.BaseEditorFragment
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class InventoryReportEditorFragment: BaseEditorFragment(), FragmentResultListener {
    private var _binding: FragmentEditorInventoryReportBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val inventoryAdapter = InventoryItemAdapter()
    private val editorViewModel: InventoryReportEditorViewModel by viewModels()

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
            onNavigationClicked = { controller?.navigateUp() },
            customTitleView = binding.appBar.toolbarTitleTextView
        )

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = inventoryAdapter
        }

        registerForFragmentResult(arrayOf(AssetPickerBottomSheet.REQUEST_KEY_PICK), this)
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
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

        binding.yearMonthTextInputLayout.setEndIconOnClickListener(::invokeYearMonthPicker)
        binding.yearMonthTextInput.setOnClickListener(::invokeYearMonthPicker)
        binding.accountabilityDateTextInputLayout.setEndIconOnClickListener(::invokeDateTimePicker)
        binding.accountabilityDateTextInput.setOnClickListener(::invokeDateTimePicker)
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {

    }

    private fun invokeDateTimePicker(view: View) {
        MaterialDialog(requireContext()).show {
            lifecycleOwner(viewLifecycleOwner)
            datePicker { _, calendar ->
                editorViewModel.inventoryReport.accountabilityDate = calendar.toTimestamp()

                val formatter = getDateFormatter(isShort = true, withYear = true)
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
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH)
        ).build().show()
    }
}