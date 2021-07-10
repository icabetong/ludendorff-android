package io.capstone.keeper.android.features.asset.specs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.capstone.keeper.android.databinding.FragmentEditorSpecificationBinding
import io.capstone.keeper.android.features.shared.components.BaseBottomSheet

class SpecificationBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentEditorSpecificationBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorSpecificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"

        const val EXTRA_SPECIFICATION_NAME = "extra:name"
        const val EXTRA_SPECIFICATION_VALUE = "extra:value"
    }
}