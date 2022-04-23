package io.capstone.ludendorff.features.inventory.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.components.utils.IntegerInputFilter
import io.capstone.ludendorff.databinding.FragmentEditorInventoryItemBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.shared.BaseBottomSheet
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class InventoryItemEditorBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentEditorInventoryItemBinding? = null
    private var requestKey: String = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val viewModel: InventoryItemEditorViewModel by viewModels()
    private val formatter = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("PHP")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorInventoryItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getParcelable<Asset>(EXTRA_ASSET)?.let {
            viewModel.asset = it

            binding.descriptionTextView.text = it.description
            binding.unitValueTextInput.setText(it.unitValue.toString())
        }
        arguments?.getParcelable<InventoryItem>(EXTRA_INVENTORY_ITEM)?.let {
            requestKey = REQUEST_KEY_UPDATE
            viewModel.inventoryItem = it
            viewModel.recompute()

            binding.descriptionTextView.text = it.description
            binding.unitValueTextInput.setText(it.unitValue.toString())
            binding.balancePerCardTextInput.setText(it.balancePerCard.toString())
            binding.onHandCountTextInput.setText(it.onHandCount.toString())
            binding.supplierTextInput.setText(it.supplier)
        }

        binding.actionButton.setOnClickListener {
            setFragmentResult(requestKey,
                bundleOf(EXTRA_INVENTORY_ITEM to viewModel.inventoryItem)
            )
            this.dismiss()
        }

        binding.balancePerCardTextInput.filters = arrayOf(IntegerInputFilter.instance)
        binding.onHandCountTextInput.filters = arrayOf(IntegerInputFilter.instance)
        binding.unitValueTextInput.doAfterTextChanged {
            viewModel.triggerUnitValue(it.toString())
        }
        binding.balancePerCardTextInput.doAfterTextChanged {
            viewModel.triggerBalancePerCardChanged(it.toString())
        }
        binding.onHandCountTextInput.doAfterTextChanged {
            viewModel.triggerOnHandCountChanged(it.toString())
        }
        binding.supplierTextInput.doAfterTextChanged {
            viewModel.triggerSupplierChanged(it.toString())
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.totalValue.observe(viewLifecycleOwner) {
            binding.totalValueTextInput.setText(formatter.format(it))
        }
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"
        const val EXTRA_ASSET = "extra:asset"
        const val EXTRA_INVENTORY_ITEM = "extra:inventory"
    }
}