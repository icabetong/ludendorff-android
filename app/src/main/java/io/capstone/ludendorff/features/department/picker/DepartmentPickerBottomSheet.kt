package io.capstone.ludendorff.features.department.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.components.custom.GenericItemDecoration
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentPickerDepartmentBinding
import io.capstone.ludendorff.features.department.Department
import io.capstone.ludendorff.features.department.DepartmentAdapter
import io.capstone.ludendorff.features.department.DepartmentViewModel
import io.capstone.ludendorff.features.shared.components.BaseBottomSheet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
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

    override fun onStart() {
        super.onStart()

        viewLifecycleOwner.lifecycleScope.launch {
            departmentAdapter.loadStateFlow.collectLatest {
                when (it.refresh) {
                    is LoadState.Loading -> {
                        binding.progressIndicator.show()
                        binding.recyclerView.hide()
                    }
                    is LoadState.Error -> {
                        binding.emptyView.root.hide()
                        binding.errorView.root.hide()
                        binding.progressIndicator.hide()
                        binding.recyclerView.hide()

                        val errorState = when {
                            it.prepend is LoadState.Error -> it.prepend as LoadState.Error
                            it.append is LoadState.Error -> it.append as LoadState.Error
                            it.refresh is LoadState.Error -> it.refresh as LoadState.Error
                            else -> null
                        }

                        errorState?.let { e ->
                            /**
                             *  Check if the error that have returned is
                             *  EmptySnapshotException, which is used if
                             *  QuerySnapshot is empty. Therefore, we
                             *  will check if the adapter is also empty
                             *  and show the user the empty state.
                             */
                            if (e.error is EmptySnapshotException &&
                                departmentAdapter.itemCount < 1)
                                binding.emptyView.root.show()
                            else binding.errorView.root.show()
                        }
                    }
                    is LoadState.NotLoading -> {
                        binding.progressIndicator.hide()
                        binding.recyclerView.show()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.departments.collectLatest {
                departmentAdapter.submitData(it)
            }
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