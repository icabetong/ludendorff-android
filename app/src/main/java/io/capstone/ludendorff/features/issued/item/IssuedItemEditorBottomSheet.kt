package io.capstone.ludendorff.features.issued.item

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
import io.capstone.ludendorff.databinding.FragmentEditorIssuedItemBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.shared.BaseBottomSheet

@AndroidEntryPoint
class IssuedItemEditorBottomSheet(fragmentManager: FragmentManager): BaseBottomSheet(fragmentManager) {
    private var _binding: FragmentEditorIssuedItemBinding? = null
    private var requestKey: String = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val viewModel: IssuedItemEditorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorIssuedItemBinding.inflate(inflater, container, false)
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
        }

        arguments?.getParcelable<IssuedItem>(EXTRA_ISSUED_ITEM)?.let {
            requestKey = REQUEST_KEY_UPDATE
            viewModel.issuedItem = it

            binding.descriptionTextView.text = it.description
            binding.quantityIssuedTextInput.setText(it.quantityIssued.toString())
            binding.responsibilityCenterTextInput.setText(it.responsibilityCenter)
        }

        binding.actionButton.setOnClickListener {
            setFragmentResult(requestKey,
                bundleOf(EXTRA_ISSUED_ITEM to viewModel.issuedItem))

            this.dismiss()
        }

        binding.quantityIssuedTextInput.doAfterTextChanged {
            viewModel.triggerQuantityIssuedChanged(it.toString())
        }
        binding.responsibilityCenterTextInput.doAfterTextChanged {
            viewModel.triggerResponsibilityCenterChanged(it.toString())
        }
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"
        const val EXTRA_ASSET = "extra:asset"
        const val EXTRA_ISSUED_ITEM = "extra:issued"
    }
}