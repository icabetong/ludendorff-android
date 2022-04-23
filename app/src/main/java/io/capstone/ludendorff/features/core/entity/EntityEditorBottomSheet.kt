package io.capstone.ludendorff.features.core.entity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.FragmentEditorEntityBinding
import io.capstone.ludendorff.features.shared.BaseBottomSheet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EntityEditorBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentEditorEntityBinding? = null

    private val binding get() = _binding!!
    private val viewModel: EntityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorEntityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            val name = binding.entityNameTextInput.text?.toString()
            val position = binding.entityPositionTextInput.text?.toString()

            if (name.isNullOrBlank()) {
                binding.entityNameTextInputLayout.error = getString(R.string.feedback_empty_entity_name)
                return@setOnClickListener
            }
            if (position.isNullOrBlank()) {
                binding.entityPositionTextInputLayout.error = getString(R.string.feedback_empty_entity_position)
                return@setOnClickListener
            }

            viewModel.update(Entity(name, position))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entity.collect {
                binding.entityNameTextInput.setText(it?.entityName)
                binding.entityPositionTextInput.setText(it?.entityPosition)
            }
        }
    }

}