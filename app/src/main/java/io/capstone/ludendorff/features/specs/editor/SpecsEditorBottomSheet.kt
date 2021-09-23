package io.capstone.ludendorff.features.specs.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.FragmentEditorSpecificationBinding
import io.capstone.ludendorff.features.shared.BaseBottomSheet

class SpecsEditorBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentEditorSpecificationBinding? = null
    private var requestKey = REQUEST_KEY_CREATE

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorSpecificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            requestKey = REQUEST_KEY_UPDATE
            binding.componentHeaderTextView.setText(R.string.title_specs_update)

            it.getString(EXTRA_KEY)?.let { key ->
                binding.nameTextInput.setText(key)
            }
            it.getString(EXTRA_VALUE)?.let { value ->
                binding.valueTextInput.setText(value)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        binding.nameTextInput.doAfterTextChanged {
            binding.actionButton.isEnabled = it.toString().isNotBlank() &&
                    binding.valueTextInput.text.toString().isNotBlank()
        }
        binding.valueTextInput.doAfterTextChanged {
            binding.actionButton.isEnabled = it.toString().isNotBlank() &&
                    binding.nameTextInput.text.toString().isNotBlank()
        }

        binding.actionButton.setOnClickListener {
            val key = binding.nameTextInput.text.toString()
            val value = binding.valueTextInput.text.toString()

            setFragmentResult(requestKey,
                bundleOf(EXTRA_KEY to key, EXTRA_VALUE to value))
            this.dismiss()
        }
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"

        const val EXTRA_KEY = "extra:key"
        const val EXTRA_VALUE = "extra:value"
    }
}