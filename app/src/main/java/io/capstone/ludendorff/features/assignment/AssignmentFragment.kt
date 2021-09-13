package io.capstone.ludendorff.features.assignment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.api.DeshiException
import io.capstone.ludendorff.components.custom.GenericItemDecoration
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentAssignmentBinding
import io.capstone.ludendorff.features.assignment.editor.AssignmentEditorFragment
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.search.SearchFragment
import io.capstone.ludendorff.features.shared.components.BaseFragment
import io.capstone.ludendorff.features.user.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AssignmentFragment: BaseFragment(), BaseFragment.CascadeMenuDelegate,
    OnItemActionListener<Assignment> {
    private var _binding: FragmentAssignmentBinding? = null
    private var controller: NavController? = null
    private var mainController: NavController? = null

    private val binding get() = _binding!!
    private val assignmentAdapter = AssignmentAdapter(this)
    private val viewModel: AssignmentViewModel by activityViewModels()
    private val coreViewModel: CoreViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val drawer = getNavigationDrawer()
                    if (drawer?.isDrawerOpen(GravityCompat.START) == true)
                        drawer.closeDrawer(GravityCompat.START)
                    else controller?.navigateUp()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssignmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(
            binding.root, binding.appBar.toolbar, arrayOf(binding.swipeRefreshLayout, binding.emptyView.root,
                binding.errorView.root, binding.permissionView.root, binding.shimmerFrameLayout),
            binding.actionButton
        )

        binding.actionButton.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_assignment,
            iconRes = R.drawable.ic_hero_menu,
            onNavigationClicked = { triggerNavigationDrawer() },
            menuRes = R.menu.menu_core_assignments,
            onMenuOptionClicked = ::onMenuItemClicked
        )

        with(binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = assignmentAdapter
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
        mainController = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

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
            assignmentAdapter.loadStateFlow.collectLatest {
                binding.swipeRefreshLayout.isRefreshing = false

                when (it.refresh) {
                    /**
                     *  The current data is loading, we
                     *  will show the user the progress indicators
                     */
                    is LoadState.Loading -> {
                        binding.recyclerView.hide()
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

                            if (e.error is EmptySnapshotException) {
                                binding.emptyView.root.show()
                            } else if (e.error is FirebaseFirestoreException &&
                                (e.error as FirebaseFirestoreException).code ==
                                FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                binding.permissionView.root.show()
                            }
                            else binding.errorView.root.show()
                        }
                    }
                    is LoadState.NotLoading -> {
                        binding.recyclerView.show()
                        binding.shimmerFrameLayout.hide()
                        binding.shimmerFrameLayout.stopShimmer()

                        binding.permissionView.root.hide()
                        binding.errorView.root.hide()
                        binding.emptyView.root.hide()
                        if (it.refresh.endOfPaginationReached)
                            binding.emptyView.root.isVisible = assignmentAdapter.itemCount < 1
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.action.collectLatest {
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
                        } else if (it.throwable is DeshiException) {
                            when(it.throwable.code) {
                                DeshiException.Code.UNAUTHORIZED -> {
                                    MaterialDialog(requireContext()).show {
                                        lifecycleOwner(viewLifecycleOwner)
                                        title(R.string.error_auth_failed)
                                        message(R.string.error_auth_failed_no_token)
                                        positiveButton()
                                    }
                                }
                                DeshiException.Code.FORBIDDEN -> {
                                    MaterialDialog(requireContext()).show {
                                        lifecycleOwner(viewLifecycleOwner)
                                        title(R.string.error_no_permission)
                                        message(R.string.error_no_permission_summary_write)
                                        positiveButton()
                                    }
                                }
                                else -> showGenericError(it.action)
                            }
                        } else
                            showGenericError(it.action)
                    }
                    is Response.Success -> {
                        when(it.data) {
                            Response.Action.CREATE ->
                                createSnackbar(R.string.feedback_assignment_created,
                                    binding.actionButton)
                            Response.Action.UPDATE ->
                                createSnackbar(R.string.feedback_assignment_updated,
                                    binding.actionButton)
                            Response.Action.REMOVE ->
                                createSnackbar(R.string.feedback_assignment_removed,
                                    binding.actionButton)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.assignments.collectLatest {
                assignmentAdapter.submitData(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            mainController?.navigate(R.id.navigation_editor_assignment, null, null,
                FragmentNavigatorExtras(it to TRANSITION_NAME_ROOT))
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            assignmentAdapter.refresh()
        }
    }

    override fun onActionPerformed(
        data: Assignment?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        if (action == OnItemActionListener.Action.SELECT) {
            container?.let {
                mainController?.navigate(R.id.navigation_editor_assignment,
                    bundleOf(AssignmentEditorFragment.EXTRA_ASSIGNMENT to data),
                    null,
                    FragmentNavigatorExtras(
                        it to TRANSITION_NAME_ROOT + data?.assignmentId
                    )
                )
            }
        }
    }

    override fun onMenuItemClicked(id: Int) {
        when(id) {
            R.id.action_search ->
                mainController?.navigate(R.id.navigation_search,
                    bundleOf(SearchFragment.EXTRA_SEARCH_COLLECTION to
                        SearchFragment.COLLECTION_ASSIGNMENTS))
        }
    }

    private fun showGenericError(action: Response.Action?) {
        when(action) {
            Response.Action.CREATE ->
                createSnackbar(R.string.feedback_assignment_create_error, binding.actionButton)
            Response.Action.UPDATE ->
                createSnackbar(R.string.feedback_assignment_update_error, binding.actionButton)
            Response.Action.REMOVE ->
                createSnackbar(R.string.feedback_assignment_remove_error, binding.actionButton)
            else -> {}
        }
    }
}