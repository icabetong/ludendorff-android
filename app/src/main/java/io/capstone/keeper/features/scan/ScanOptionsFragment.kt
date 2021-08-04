package io.capstone.keeper.features.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import io.capstone.keeper.components.extensions.hide
import io.capstone.keeper.components.extensions.show
import io.capstone.keeper.databinding.FragmentOptionsScanBinding
import io.capstone.keeper.features.core.backend.Response
import io.capstone.keeper.features.shared.components.BaseFragment

class ScanOptionsFragment: BaseFragment() {
    private var _binding: FragmentOptionsScanBinding? = null

    private val binding get() = _binding!!
    private val viewModel: ScanViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOptionsScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
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
                    binding.nestedScrollView.hide()
                }
                is Response.Success -> {
                    binding.decodeErrorView.root.hide()
                    binding.nestedScrollView.show()

                    response.data.let {
                        binding.assetNameTextView.text = it.assetName
                        binding.categoryTextView.text = it.category?.categoryName

                        it.status?.let { status ->
                            binding.assetStatusTextView.setText(status.getStringRes())
                        }
                    }
                }
            }
        }

        viewModel.assetId.observe(viewLifecycleOwner) {
            binding.emptyView.root.isVisible = it.isNullOrBlank()
        }
    }

}