package io.capstone.keeper.features.department

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.paging.LoadState
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.components.custom.GenericItemDecoration
import io.capstone.keeper.components.custom.SwipeItemCallback
import io.capstone.keeper.components.exceptions.EmptySnapshotException
import io.capstone.keeper.components.extensions.getCountThatFitsOnScreen
import io.capstone.keeper.components.extensions.hide
import io.capstone.keeper.components.extensions.setup
import io.capstone.keeper.components.extensions.show
import io.capstone.keeper.components.interfaces.OnItemActionListener
import io.capstone.keeper.databinding.FragmentDepartmentBinding
import io.capstone.keeper.features.department.editor.DepartmentEditorFragment
import io.capstone.keeper.features.shared.components.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DepartmentFragment: BaseFragment(), OnItemActionListener<Department> {
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
        binding.actionButton.transitionName = TRANSITION_NAME_ROOT

        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_department,
            onNavigationClicked = { controller?.navigateUp() }
        )

        with(binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = departmentAdapter
        }

        ItemTouchHelper(SwipeItemCallback(view.context, departmentAdapter))
            .attachToRecyclerView(binding.recyclerView)

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

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()

        viewLifecycleOwner.lifecycleScope.launch {
            departmentAdapter.loadStateFlow.collectLatest {
                binding.swipeRefreshLayout.isRefreshing = false

                when (it.refresh) {
                    /**
                     *  The current data is loading, we
                     *  will show the user the progress indicators
                     */
                    is LoadState.Loading -> {
                        binding.recyclerView.hide()
                        binding.skeletonLayout.show()
                        binding.shimmerFrameLayout.show()
                        binding.shimmerFrameLayout.startShimmer()

                        binding.errorPermissionsView.root.hide()
                        binding.errorView.root.hide()
                        binding.emptyView.root.hide()
                    }
                    /**
                     *  The PagingAdapter or any component related to fetch
                     *  the data have encountered an exception. Refer to the
                     *  CategoryPagingSource class to determine the logic
                     *  used in handling different types of errors.
                     */
                    is LoadState.Error -> {
                        binding.recyclerView.hide()
                        binding.skeletonLayout.hide()
                        binding.shimmerFrameLayout.hide()

                        val errorState = when {
                            it.prepend is LoadState.Error -> it.prepend as LoadState.Error
                            it.append is LoadState.Error -> it.append as LoadState.Error
                            it.refresh is LoadState.Error -> it.refresh as LoadState.Error
                            else -> null
                        }

                        binding.errorView.root.hide()
                        binding.errorPermissionsView.root.hide()
                        binding.emptyView.root.hide()

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
                            else if (e.error is FirebaseFirestoreException) {
                                when((e.error as FirebaseFirestoreException).code) {
                                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                                        binding.errorPermissionsView.root.show()
                                    else -> binding.errorView.root.show()
                                }
                            } else binding.errorView.root.show()
                        }
                    }
                    is LoadState.NotLoading -> {
                        binding.recyclerView.show()
                        binding.skeletonLayout.hide()
                        binding.shimmerFrameLayout.hide()
                        binding.shimmerFrameLayout.stopShimmer()

                        binding.errorView.root.hide()
                        binding.errorPermissionsView.root.hide()
                        binding.emptyView.root.hide()
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

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            controller?.navigate(R.id.navigation_editor_department, null, null,
                FragmentNavigatorExtras(it to TRANSITION_NAME_ROOT))
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            departmentAdapter.refresh()
        }
    }

    override fun onActionPerformed(
        data: Department?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        when(action) {
            OnItemActionListener.Action.SELECT -> {
                container?.let {
                    controller?.navigate(R.id.navigation_editor_department,
                        bundleOf(DepartmentEditorFragment.EXTRA_DEPARTMENT to data), null,
                        FragmentNavigatorExtras(
                            container to TRANSITION_NAME_ROOT + data?.departmentId
                        )
                    )
                }
            }
            OnItemActionListener.Action.DELETE -> TODO()
        }
    }
}