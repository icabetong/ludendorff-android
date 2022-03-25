package io.capstone.ludendorff.features.home

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
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.GenericItemDecoration
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.setColorRes
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentHomeBinding
import io.capstone.ludendorff.features.assignment.Assignment
import io.capstone.ludendorff.features.assignment.AssignmentViewModel
import io.capstone.ludendorff.features.assignment.viewer.AssignmentViewer
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment: BaseFragment(), OnItemActionListener<Assignment>,
    BaseFragment.CascadeMenuDelegate {
    private var _binding: FragmentHomeBinding? = null
    private var controller: NavController? = null
    private var mainController: NavController? = null

    private val binding get() = _binding!!
    private val homeAdapter = HomeAdapter(this)
    private val viewModel: HomeViewModel by activityViewModels()
    private val requestViewModel: RequestViewModel by activityViewModels()
    private val assignmentViewModel: AssignmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(
            binding.root, binding.appBar.toolbar, arrayOf(binding.swipeRefreshLayout,
                binding.emptyView.root, binding.errorView.root, binding.permissionView.root,
                binding.shimmerFrameLayout),
            binding.actionButton
        )

        binding.actionButton.transitionName = TRANSITION_NAME_ROOT
        binding.swipeRefreshLayout.setColorRes(R.color.brand_primary, R.color.brand_surface)
        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_home,
            iconRes = R.drawable.ic_round_menu_24,
            onNavigationClicked = { triggerNavigationDrawer() },
            menuRes = R.menu.menu_core_home,
            onMenuOptionClicked = ::onMenuItemClicked
        )

        with(binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = homeAdapter
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
        mainController = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        viewLifecycleOwner.lifecycleScope.launch {
            assignmentViewModel.assignment.collectLatest {
                if (it is Response.Success)
                    AssignmentViewer(childFragmentManager).show {
                        arguments = bundleOf(AssignmentViewer.EXTRA_ASSIGNMENT to it.data)
                    }
                else createSnackbar(R.string.error_generic)
            }
        }

        /**
         *  Use Kotlin's coroutines to fetch the current loadState of
         *  the PagingAdapter; we will use the viewLifecycleOwner to
         *  avoid memory leaks as we are using fragments as the presenter.
         */
        viewLifecycleOwner.lifecycleScope.launch {
            homeAdapter.loadStateFlow.collectLatest {
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

                        errorState?.let { e ->
                            /**
                             *  Check if the error that have returned is
                             *  EmptySnapshotException, which is used if
                             *  QuerySnapshot is empty. Therefore, we
                             *  will check if the adapter is also empty
                             *  and show the user the empty state.
                             */
                            binding.permissionView.root.hide()
                            binding.errorView.root.hide()
                            binding.emptyView.root.hide()

                            if (e.error is EmptySnapshotException &&
                                homeAdapter.itemCount < 1) {
                                binding.emptyView.root.show()
                            } else if (e.error is FirebaseFirestoreException) {
                                when((e.error as FirebaseFirestoreException).code) {
                                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                                        binding.permissionView.root.show()
                                    else -> binding.errorView.root.show()
                                }
                            }
                            else binding.errorView.root.show()
                        }
                    }
                    is LoadState.NotLoading -> {
                        binding.recyclerView.show()
                        binding.skeletonLayout.hide()
                        binding.shimmerFrameLayout.hide()
                        binding.shimmerFrameLayout.stopShimmer()

                        binding.permissionView.root.hide()
                        binding.errorView.root.hide()
                        binding.emptyView.root.hide()
                        if (it.refresh.endOfPaginationReached)
                            binding.emptyView.root.isVisible = homeAdapter.itemCount < 1
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            requestViewModel.action.collectLatest {
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
                                    createSnackbar(R.string.feedback_request_create_error,
                                        binding.actionButton)
                                Response.Action.REMOVE ->
                                    createSnackbar(R.string.feedback_request_remove_error,
                                        binding.actionButton)
                                else -> {}
                            }
                        }
                    }
                    is Response.Success -> {
                        when(it.data) {
                            Response.Action.CREATE ->
                                createSnackbar(R.string.feedback_request_created,
                                    binding.actionButton)
                            Response.Action.REMOVE ->
                                createSnackbar(R.string.feedback_request_removed,
                                    binding.actionButton)
                            else -> {}
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.assignments.collectLatest {
                homeAdapter.submitData(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {

        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            homeAdapter.refresh()
        }
    }

    override fun onActionPerformed(
        data: Assignment?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        if (action == OnItemActionListener.Action.SELECT) {
            AssignmentViewer(childFragmentManager).show {
                arguments = bundleOf(AssignmentViewer.EXTRA_ASSIGNMENT to data)
            }
        }
    }

    override fun onMenuItemClicked(id: Int) {
        when(id) {

        }
    }
}