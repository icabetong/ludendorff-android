package io.capstone.keeper.features.department

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.paging.LoadState
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.components.custom.GenericItemDecoration
import io.capstone.keeper.components.exceptions.EmptySnapshotException
import io.capstone.keeper.components.extensions.getCountThatFitsOnScreen
import io.capstone.keeper.databinding.FragmentDepartmentBinding
import io.capstone.keeper.features.department.editor.DepartmentEditorFragment
import io.capstone.keeper.features.shared.components.BaseFragment
import io.capstone.keeper.features.shared.components.BasePagingAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DepartmentFragment: BaseFragment(), BasePagingAdapter.OnItemActionListener {
    private var _binding: FragmentDepartmentBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: DepartmentViewModel by viewModels()
    private val departmentAdapter = DepartmentAdapter(this)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
        setupToolbar(binding.appBar.toolbar, {
            controller?.navigateUp()
        }, R.string.activity_department)
    }

    override fun onStart() {
        super.onStart()

        with (binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = departmentAdapter
        }

        binding.rowLayout.root.doOnLayout {
            val rowCount = it.getCountThatFitsOnScreen(it.context)
            binding.skeletonLayout.removeView(it)

            for (i in 0 until rowCount) {
                val row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.layout_item_department_skeleton,
                        binding.skeletonLayout, false) as ViewGroup
                binding.skeletonLayout.addView(row)
                row.requestLayout()
            }
        }

        binding.actionButton.setOnClickListener {
            DepartmentEditorFragment(childFragmentManager)
                .show()
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            departmentAdapter.refresh()
        }
    }

    override fun onResume() {
        super.onResume()

        viewLifecycleOwner.lifecycleScope.launch {
            departmentAdapter.loadStateFlow.collectLatest {
                binding.swipeRefreshLayout.isRefreshing = false

                when (it.refresh) {
                    /**
                     *  The current data is loading, we
                     *  will show the user the progress indicators
                     */
                    is LoadState.Loading -> {
                        binding.recyclerView.isVisible = false
                        binding.skeletonLayout.isVisible = true
                        binding.shimmerFrameLayout.isVisible = true
                        binding.shimmerFrameLayout.startShimmer()

                        hideStatusViews()
                    }
                    /**
                     *  The PagingAdapter or any component related to fetch
                     *  the data have encountered an exception. Refer to the
                     *  CategoryPagingSource class to determine the logic
                     *  used in handling different types of errors.
                     */
                    is LoadState.Error -> {
                        binding.recyclerView.isVisible = false
                        binding.skeletonLayout.isVisible = false
                        binding.shimmerFrameLayout.isVisible = false

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
                            hideStatusViews()
                            if (e.error is EmptySnapshotException &&
                                    departmentAdapter.itemCount < 1)
                                binding.emptyView.root.isVisible = true
                            else if (e.error is FirebaseFirestoreException) {
                                when((e.error as FirebaseFirestoreException).code) {
                                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                                        binding.errorPermissionsView.root.isVisible = true
                                    else -> binding.errorView.root.isVisible = true
                                }
                            } else binding.errorView.root.isVisible = true
                        }
                    }
                    is LoadState.NotLoading -> {
                        binding.recyclerView.isVisible = true
                        binding.skeletonLayout.isVisible = false
                        binding.shimmerFrameLayout.isVisible = false
                        binding.shimmerFrameLayout.stopShimmer()

                        hideStatusViews()
                        if (it.refresh.endOfPaginationReached)
                            binding.emptyView.root.isVisible = departmentAdapter.itemCount < 1
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

    private fun hideStatusViews() {
        binding.errorView.root.isVisible = false
        binding.errorPermissionsView.root.isVisible = false
        binding.emptyView.root.isVisible = false
    }

    override fun <T> onActionPerformed(t: T, action: BasePagingAdapter.Action) {

    }
}