package io.capstone.keeper.features.department.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import io.capstone.keeper.components.custom.GenericItemDecoration
import io.capstone.keeper.components.interfaces.OnItemActionListener
import io.capstone.keeper.databinding.FragmentPickerDepartmentBinding
import io.capstone.keeper.features.department.Department
import io.capstone.keeper.features.department.DepartmentAdapter
import io.capstone.keeper.features.department.DepartmentViewModel
import io.capstone.keeper.features.shared.components.BaseBottomSheet

class DepartmentPickerBottomSheet(manager: FragmentManager): BaseBottomSheet(manager),
    OnItemActionListener<Department> {
    private var _binding: FragmentPickerDepartmentBinding? = null

    private val binding get() = _binding!!
    private val viewModel: DepartmentViewModel by viewModels()
    private val departmentAdapter = DepartmentAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickerDepartmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = departmentAdapter
        }
    }

    override fun onActionPerformed(
        data: Department?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        if (action == OnItemActionListener.Action.SELECT)
            setFragmentResult(REQUEST_KEY_PICK, bundleOf(EXTRA_DEPARTMENT to data))
        this.dismiss()
    }

    companion object {
        const val REQUEST_KEY_PICK = "request:pick"
        const val EXTRA_DEPARTMENT = "extra:department"
    }
}