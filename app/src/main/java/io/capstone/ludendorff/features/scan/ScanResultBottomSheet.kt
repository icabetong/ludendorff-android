package io.capstone.ludendorff.features.scan

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.databinding.FragmentViewResultsBinding
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.BaseBottomSheet
import java.text.NumberFormat
import java.util.*

class ScanResultBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {

    private var _binding: FragmentViewResultsBinding? = null

    private val binding get() = _binding!!
    private val viewModel: ScanViewModel by activityViewModels()
    private val formatter = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("PHP")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.emptyView.root.show()
    }

    override fun onStart() {
        super.onStart()

        viewModel.asset.observe(viewLifecycleOwner) { response ->
            when(response) {
                is Response.Error -> {
                    binding.decodeErrorView.root.show()
                    binding.detailsLayout.hide()
                    binding.progressIndicator.hide()
                }
                is Response.Success -> {
                    binding.decodeErrorView.root.hide()
                    binding.progressIndicator.hide()
                    binding.detailsLayout.show()

                    response.data.let {
                        binding.stockNumberTextView.text = it.stockNumber
                        binding.assetNameTextView.text = it.description
                        binding.typeTextView.text = if (it.type != null) it.type?.typeName
                            else getString(R.string.error_unknown)
                        binding.classificationTextView.text = it.classification
                        binding.unitOfMeasureTextView.text = it.unitOfMeasure
                        binding.unitValueTextView.text = formatter.format(it.unitValue)
                    }
                }
            }
        }

        viewModel.assetId.observe(viewLifecycleOwner) {
            binding.progressIndicator.isVisible = it != null
            binding.detailsLayout.isVisible = it != null
            binding.emptyView.root.isVisible = it == null
        }
    }
}