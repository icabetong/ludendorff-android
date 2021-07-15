package io.capstone.keeper.features.department

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.keeper.databinding.FragmentDepartmentBinding
import io.capstone.keeper.features.shared.components.BaseFragment

class DepartmentFragment: BaseFragment() {
    private var _binding: FragmentDepartmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDepartmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}