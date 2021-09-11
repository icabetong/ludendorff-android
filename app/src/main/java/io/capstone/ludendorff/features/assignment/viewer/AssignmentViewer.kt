package io.capstone.ludendorff.features.assignment.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.capstone.ludendorff.databinding.FragmentViewAssignmentBinding
import io.capstone.ludendorff.features.assignment.Assignment
import io.capstone.ludendorff.features.shared.components.BaseBottomSheet

class AssignmentViewer(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentViewAssignmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewAssignmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getParcelable<Assignment>(EXTRA_ASSIGNMENT)?.let {
            binding.assetNameTextView.text = it.asset?.assetName
            binding.categoryTextView.text = it.asset?.category?.categoryName
            binding.dateAssignedTextView.text = it.formatDateAssigned(view.context)

            it.asset?.status?.getStringRes()?.let { res ->
                binding.assetStatusTextView.setText(res)
            }
        }
    }

    companion object {
        const val EXTRA_ASSIGNMENT = "extra:assignment"
    }

}