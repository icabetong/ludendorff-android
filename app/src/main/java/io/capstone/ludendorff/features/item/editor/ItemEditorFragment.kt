package io.capstone.ludendorff.features.item.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputEditText
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.FragmentEditorItemBinding
import io.capstone.ludendorff.features.shared.BaseEditorFragment

class ItemEditorFragment: BaseEditorFragment(), View.OnFocusChangeListener {
    private var _binding: FragmentEditorItemBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = buildContainerTransform()
        sharedElementReturnTransition = buildContainerTransform()

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    controller?.navigateUp()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorItemBinding.inflate(inflater, container, false);
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.toolbar, arrayOf(binding.remarksTextInputLayout))
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
    }

    override fun onStop() {
        super.onStop()
        hideKeyboardFromCurrentFocus(binding.root)
    }

    override fun onResume() {
        super.onResume()

        binding.stockNumberTextInput.onFocusChangeListener = this
        binding.descriptionTextInput.onFocusChangeListener = this
        binding.classificationTextInput.onFocusChangeListener = this
        binding.unitOfMeasureTextInput.onFocusChangeListener = this
        binding.unitValueTextInput.onFocusChangeListener = this
        binding.remarksTextInput.onFocusChangeListener = this
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v is TextInputEditText) {
            if (hasFocus) {
                val placeholderTextRes: Int = when(v.id) {
                    R.id.stockNumberTextInput -> R.string.placeholder_stock_number
                    R.id.descriptionTextInput -> R.string.placeholder_item_description
                    R.id.classificationTextInput -> R.string.placeholder_item_classification
                    R.id.unitOfMeasureTextInput -> R.string.placeholder_item_unit_of_measure
                    R.id.unitValueTextInput -> R.string.placeholder_item_unit_value
                    R.id.remarksTextInput -> R.string.placeholder_item_remarks
                    else -> -1
                }
                val placeholder: String? = if (placeholderTextRes != -1)
                    getString(placeholderTextRes)
                else null

                v.hint = placeholder
            } else {
                v.hint = null
            }
        }
    }

}