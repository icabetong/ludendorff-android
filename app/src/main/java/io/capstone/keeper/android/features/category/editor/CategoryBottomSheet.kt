package io.capstone.keeper.android.features.category.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.capstone.keeper.android.databinding.FragmentEditorCategoryBinding
import io.capstone.keeper.android.features.shared.components.BaseBottomSheet

class CategoryBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentEditorCategoryBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"

        const val EXTRA_CATEGORY = "extra:category"
    }
}