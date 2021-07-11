package io.capstone.keeper.android.features.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import io.capstone.keeper.android.databinding.FragmentScanResultBinding
import io.capstone.keeper.android.features.shared.components.BaseFragment

class ScanResultFragment: BaseFragment() {
    private var _binding: FragmentScanResultBinding? = null

    private val binding get() = _binding!!
    private val viewModel: ScanViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        viewModel.decodeResult.observe(viewLifecycleOwner) {
            binding.resultTextView.text = it
        }
    }
}