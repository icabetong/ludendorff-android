package io.capstone.ludendorff.features.request.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import io.capstone.ludendorff.databinding.FragmentViewRequestBinding
import io.capstone.ludendorff.features.request.Request
import io.capstone.ludendorff.features.request.RequestViewModel
import io.capstone.ludendorff.features.shared.components.BaseBottomSheet

class RequestViewerBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentViewRequestBinding? = null
    private var request: Request? = null

    private val binding get() = _binding!!
    private val viewModel: RequestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        request = arguments?.getParcelable<Request>(EXTRA_REQUEST)?.let {
            binding.assetNameTextView.text = it.asset?.assetName
            binding.categoryTextView.text = it.asset?.category?.categoryName
            binding.petitionerTextView.text = it.petitioner?.name

            it.asset?.status?.getStringRes()?.let { res ->
                binding.assetStatusTextView.setText(res)
            }
            it
        }
    }

    override fun onResume() {
        super.onResume()

        binding.cancelButton.setOnClickListener {
            this.dismiss()
        }
        binding.endorseButton.setOnClickListener {
            request?.let {
                viewModel.endorse(it)
                this.dismiss()
            }
        }
    }

    companion object {
        const val EXTRA_REQUEST = "extra:request"
    }
}