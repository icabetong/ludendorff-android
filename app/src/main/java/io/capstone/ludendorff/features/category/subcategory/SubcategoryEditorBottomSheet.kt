package io.capstone.ludendorff.features.category.subcategory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.FragmentEditorSubcategoryBinding
import io.capstone.ludendorff.features.shared.BaseBottomSheet

class SubcategoryEditorBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentEditorSubcategoryBinding? = null
    private var requestKey: String = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val viewModel: SubcategoryEditorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorSubcategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(EXTRA_SUBCATEGORY)?.let {
            requestKey = REQUEST_KEY_UPDATE
            viewModel.subcategoryName = it

            binding.componentHeaderTextView.setText(R.string.title_subcategory_update)
            binding.nameTextInput.setText(it)
        }

        binding.actionButton.setOnClickListener {
            setFragmentResult(requestKey,
                bundleOf(EXTRA_SUBCATEGORY to viewModel.subcategoryName))

            this.dismiss()
        }
        binding.nameTextInput.doAfterTextChanged { viewModel.triggerNameChanged(it.toString()) }
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"

        const val EXTRA_SUBCATEGORY = "extra:subcategory"
    }
}