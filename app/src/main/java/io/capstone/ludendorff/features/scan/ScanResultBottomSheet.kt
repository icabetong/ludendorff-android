package io.capstone.ludendorff.features.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import coil.load
import coil.transform.CircleCropTransformation
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.databinding.FragmentViewResultsBinding
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.BaseBottomSheet

class ScanResultBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentViewResultsBinding? = null

    private val binding get() = _binding!!
    private val viewModel: ScanViewModel by activityViewModels()

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

                    binding.profileImageView.setImageResource(R.drawable.ic_round_healing_24)
                    binding.userNameTextView.setText(R.string.error_assignment_not_exist_header)
                    binding.emailTextView.setText(R.string.error_assignment_not_exist_summary)

                    response.data.let {
                        binding.assetNameTextView.text = it.description
                        binding.categoryTextView.text = it.classification
                    }
                }
            }
        }

        viewModel.assetId.observe(viewLifecycleOwner) {
            binding.progressIndicator.isVisible = it != null
            binding.emptyView.root.isVisible = it == null
        }
    }
}