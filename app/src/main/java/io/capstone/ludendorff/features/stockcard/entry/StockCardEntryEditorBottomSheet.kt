package io.capstone.ludendorff.features.stockcard.entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.components.extensions.toLocalDate
import io.capstone.ludendorff.components.extensions.toTimestamp
import io.capstone.ludendorff.components.utils.DateTimeFormatter
import io.capstone.ludendorff.components.utils.IntegerInputFilter
import io.capstone.ludendorff.databinding.FragmentEditorStockCardEntryBinding
import io.capstone.ludendorff.features.inventory.InventoryReport
import io.capstone.ludendorff.features.inventory.picker.InventoryReportPickerFragment
import io.capstone.ludendorff.features.shared.BaseBottomSheet
import io.capstone.ludendorff.features.stockcard.editor.StockCardEditorViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class StockCardEntryEditorBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentEditorStockCardEntryBinding? = null
    private var requestKey: String = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val viewModel: StockCardEntryEditorViewModel by viewModels()
    private val editorViewModel: StockCardEditorViewModel by activityViewModels()
    private val formatter = DateTimeFormatter.getDateFormatter(withYear = true, isShort = true)
    private val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("PHP")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorStockCardEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getParcelable<StockCardEntry>(EXTRA_STOCK_CARD_ENTRY)?.let {
            requestKey = REQUEST_KEY_UPDATE
            viewModel.stockCardEntry = it

            binding.dateTextInput.setText(formatter.format(it.date.toLocalDate()))
            binding.receivedQuantityTextInput.setText(it.receivedQuantity.toString())
            binding.requestedQuantityTextInput.setText(it.requestedQuantity.toString())
            binding.issueQuantityTextInput.setText(it.issueQuantity.toString())
            binding.issueOfficeTextInput.setText(it.issueOffice.toString())
        }

        binding.actionButton.setOnClickListener {
            setFragmentResult(requestKey,
                bundleOf(EXTRA_STOCK_CARD_ENTRY to viewModel.stockCardEntry))
            this.dismiss()
        }

        binding.dateTextInputLayout.setEndIconOnClickListener(::onInvokeDatePicker)
        binding.dateTextInput.setOnClickListener(::onInvokeDatePicker)

        binding.receivedQuantityTextInput.filters = arrayOf(IntegerInputFilter.instance)
        binding.requestedQuantityTextInput.filters = arrayOf(IntegerInputFilter.instance)
        binding.issueQuantityTextInput.filters = arrayOf(IntegerInputFilter.instance)
        binding.balanceQuantityTextInput.filters = arrayOf(IntegerInputFilter.instance)
        binding.requestedQuantityTextInput.doAfterTextChanged {
            viewModel.triggerRequestedQuantityChanged(it.toString())
        }
        binding.issueQuantityTextInput.doAfterTextChanged {
            viewModel.triggerIssueQuantityChanged(it.toString())
        }
        binding.issueOfficeTextInput.doAfterTextChanged {
            viewModel.triggerIssueOffice(it.toString())
        }
    }

    override fun onStart() {
        super.onStart()

        editorViewModel.balanceEntries.observe(viewLifecycleOwner) {
            val stockCardEntry = viewModel.stockCardEntry

            binding.receivedQuantityTextInput.setText(stockCardEntry.receivedQuantity.toString())
            it[stockCardEntry.inventoryReportSourceId]?.let { entry ->
                val quantity = entry.entries[stockCardEntry.stockCardEntryId] ?: 0
                val total = quantity * editorViewModel.stockCard.unitPrice

                binding.balanceQuantityTextInput.setText(quantity.toString())
                binding.balanceTotalPriceTextInput.setText(currencyFormatter.format(total))
            }
        }
    }

    private fun onInvokeDatePicker(view: View) {
        MaterialDialog(requireContext()).show {
            lifecycleOwner(viewLifecycleOwner)
            datePicker { _, datetime ->
                viewModel.stockCardEntry.date = datetime.toTimestamp()
                binding.dateTextInput.setText(formatter.format(datetime.toLocalDate()))
            }
        }
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"
        const val EXTRA_STOCK_CARD_ENTRY = "extra:entry"
    }
}