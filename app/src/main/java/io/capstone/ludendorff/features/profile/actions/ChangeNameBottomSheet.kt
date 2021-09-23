package io.capstone.ludendorff.features.profile.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import io.capstone.ludendorff.databinding.FragmentChangeNameBinding
import io.capstone.ludendorff.features.shared.BaseBottomSheet

class ChangeNameBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentChangeNameBinding? = null
    private var currentFirstName: String? = null
    private var currentLastName: String? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangeNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            currentFirstName = it.getString(EXTRA_FIRST_NAME)
            currentLastName = it.getString(EXTRA_LAST_NAME)

            binding.firstNameTextInput.setText(currentFirstName)
            binding.lastNameTextInput.setText(currentLastName)
        }
    }

    override fun onStart() {
        super.onStart()

        binding.firstNameTextInput.doAfterTextChanged {
            binding.actionButton.isEnabled = it.toString() != currentFirstName &&
                    (!it.isNullOrBlank() && !binding.lastNameTextInput.text.isNullOrBlank())
        }
        binding.lastNameTextInput.doAfterTextChanged {
            binding.actionButton.isEnabled = it.toString() != currentLastName &&
                    (!it.isNullOrBlank() && !binding.firstNameTextInput.text.isNullOrBlank())
        }

        binding.actionButton.setOnClickListener {
            val firstName = binding.firstNameTextInput.text.toString()
            val lastName = binding.lastNameTextInput.text.toString()

            setFragmentResult(REQUEST_KEY_CHANGE,
                bundleOf(EXTRA_FIRST_NAME to firstName, EXTRA_LAST_NAME to lastName))
            this.dismiss()
        }
    }

    companion object {
        const val REQUEST_KEY_CHANGE = "request:change:name"
        const val EXTRA_FIRST_NAME = "extra:first"
        const val EXTRA_LAST_NAME = "extra:last"
    }
}