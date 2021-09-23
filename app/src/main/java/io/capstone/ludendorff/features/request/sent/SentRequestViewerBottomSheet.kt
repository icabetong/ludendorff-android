package io.capstone.ludendorff.features.request.sent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.FragmentViewRequestSentBinding
import io.capstone.ludendorff.features.request.Request
import io.capstone.ludendorff.features.shared.BaseBottomSheet

class SentRequestViewerBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentViewRequestSentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewRequestSentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getParcelable<Request>(EXTRA_REQUEST)?.let {
            binding.assetNameTextView.text = it.asset?.assetName
            binding.categoryTextView.text = it.asset?.category?.categoryName
            binding.endorserTextView.text = it.endorser?.name ?: getString(R.string.info_not_endorsed)

            it.asset?.status?.getStringRes()?.also { res ->
                binding.assetStatusTextView.setText(res)
            }
        }
    }

    companion object {
        const val EXTRA_REQUEST = "extra:request"
    }
}