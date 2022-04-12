package io.capstone.ludendorff.features.stockcard.entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.components.extensions.toLocalDate
import io.capstone.ludendorff.components.extensions.toTimestamp
import io.capstone.ludendorff.components.utils.DateTimeFormatter
import io.capstone.ludendorff.components.utils.IntegerInputFilter
import io.capstone.ludendorff.databinding.FragmentEditorStockCardEntryBinding
import io.capstone.ludendorff.features.shared.BaseBottomSheet

@AndroidEntryPoint
class StockCardEntryEditorBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentEditorStockCardEntryBinding? = null
    private var requestKey: String = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val viewModel: StockCardEntryEditorViewModel by viewModels()
    private val formatter = DateTimeFormatter.getDateFormatter(withYear = true, isShort = true)

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
            binding.referenceTextInput.setText(it.reference)
            binding.receiptQuantityTextInput.setText(it.receiptQuantity.toString())
            binding.requestedQuantityTextInput.setText(it.requestedQuantity.toString())
            binding.issueQuantityTextInput.setText(it.issueQuantity.toString())
            binding.issueOfficeTextInput.setText(it.issueOffice.toString())
            binding.balanceQuantityTextInput.setText(it.balanceQuantity.toString())
            binding.balanceTotalPriceTextInput.setText(it.balanceTotalPrice.toString())
        }

        binding.actionButton.setOnClickListener {
            setFragmentResult(requestKey,
                bundleOf(EXTRA_STOCK_CARD_ENTRY to viewModel.stockCardEntry))
            this.dismiss()
        }

        binding.dateTextInputLayout.setEndIconOnClickListener(::onInvokeDatePicker)
        binding.dateTextInput.setOnClickListener(::onInvokeDatePicker)

        binding.receiptQuantityTextInput.filters = arrayOf(IntegerInputFilter.instance)
        binding.requestedQuantityTextInput.filters = arrayOf(IntegerInputFilter.instance)
        binding.issueQuantityTextInput.filters = arrayOf(IntegerInputFilter.instance)
        binding.balanceQuantityTextInput.filters = arrayOf(IntegerInputFilter.instance)
        binding.referenceTextInput.doAfterTextChanged {
            viewModel.triggerReferenceChanged(it.toString())
        }
        binding.receiptQuantityTextInput.doAfterTextChanged {
            viewModel.triggerReceiptQuantityChanged(it.toString())
        }
        binding.requestedQuantityTextInput.doAfterTextChanged {
            viewModel.triggerRequestedQuantityChanged(it.toString())
        }
        binding.issueQuantityTextInput.doAfterTextChanged {
            viewModel.triggerIssueQuantityChanged(it.toString())
        }
        binding.issueOfficeTextInput.doAfterTextChanged {
            viewModel.triggerIssueOffice(it.toString())
        }
        binding.balanceQuantityTextInput.doAfterTextChanged {
            viewModel.triggerBalanceQuantity(it.toString())
        }
        binding.balanceTotalPriceTextInput.doAfterTextChanged {
            viewModel.triggerBalanceTotalPrice(it.toString())
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