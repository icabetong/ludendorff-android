package io.capstone.ludendorff.features.department

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.paging.LoadState
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.GenericItemDecoration
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentDepartmentBinding
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.department.editor.DepartmentEditorFragment
import io.capstone.ludendorff.features.shared.components.BaseFragment
import io.capstone.ludendorff.features.user.User
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DepartmentFragment: BaseFragment(), OnItemActionListener<Department> {
    private var _binding: FragmentDepartmentBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: DepartmentViewModel by activityViewModels()
    private val coreViewModel: CoreViewModel by activityViewModels()
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

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()

        coreViewModel.userData.observe(viewLifecycleOwner) {
            binding.actionButton.isVisible = it.hasPermission(User.PERMISSION_WRITE)
                    || it.hasPermission(User.PERMISSION_ADMINISTRATIVE)
        }

        /**
         *  Use Kotlin's coroutines to fetch the current loadState of
         *  the PagingAdapter; we will use the viewLifecycleOwner to
         *  avoid memory leaks as we are using fragments as the presenter.
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.action.collect {
                when(it) {
                    is Response.Error -> {
                        if (it.throwable is FirebaseFirestoreException &&
                            it.throwable.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {

                            MaterialDialog(requireContext()).show {
                                lifecycleOwner(viewLifecycleOwner)
                                title(R.string.error_no_permission)
                                message(R.string.error_no_permission_summary_write)
                                positiveButton()
                            }
                        } else {
                            when(it.action) {
                                Response.Action.CREATE ->
                                    createSnackbar(R.string.feedback_department_create_error)
                                Response.Action.UPDATE ->
                                    createSnackbar(R.string.feedback_department_update_error)
                                Response.Action.REMOVE ->
                                    createSnackbar(R.string.feedback_department_remove_error)
                                else -> {}
                            }
                        }
                    }
                    is Response.Success -> {
                        when(it.data) {
                            Response.Action.CREATE ->
                                createSnackbar(R.string.feedback_department_created)
                            Response.Action.UPDATE ->
                                createSnackbar(R.string.feedback_department_updated)
                            Response.Action.REMOVE ->
                                createSnackbar(R.string.feedback_department_removed)
                        }
                    }
                }
            }
        }

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
                     *  particular PagingSource class to determine the logic
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
        if (action == OnItemActionListener.Action.SELECT) {
            container?.let {
                controller?.navigate(R.id.navigation_editor_department,
                    bundleOf(DepartmentEditorFragment.EXTRA_DEPARTMENT to data), null,
                    FragmentNavigatorExtras(
                        it to TRANSITION_NAME_ROOT + data?.departmentId
                    )
                )
            }
        }
    }
}