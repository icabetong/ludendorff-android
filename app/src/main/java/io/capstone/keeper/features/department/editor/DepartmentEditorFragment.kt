package io.capstone.keeper.features.department.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.databinding.FragmentEditorDepartmentBinding
import io.capstone.keeper.features.shared.components.BaseBottomSheet

@AndroidEntryPoint
class DepartmentEditorFragment(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentEditorDepartmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorDepartmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}