package io.capstone.ludendorff.features.profile.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import io.capstone.ludendorff.databinding.FragmentChangePasswordBinding
import io.capstone.ludendorff.features.shared.components.BaseBottomSheet

class ChangePasswordBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentChangePasswordBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        binding.passwordTextInput.doAfterTextChanged {
            binding.actionButton.isEnabled = !it.isNullOrBlank() && (it.toString() ==
                    binding.confirmPasswordTextInput.text.toString())
        }
        binding.confirmPasswordTextInput.doAfterTextChanged {
            binding.actionButton.isEnabled = !it.isNullOrBlank() && (it.toString() ==
                    binding.passwordTextInput.text.toString())
        }

        binding.actionButton.setOnClickListener {
            setFragmentResult(REQUEST_KEY_CHANGE,
                bundleOf(EXTRA_PASSWORD to binding.passwordTextInput.text.toString()))
        }
    }

    companion object {
        const val REQUEST_KEY_CHANGE = "request:change:password"
        const val EXTRA_PASSWORD = "extra:password"
    }
}