package io.capstone.ludendorff.features.user

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
import io.capstone.ludendorff.api.exception.DeshiException
import io.capstone.ludendorff.components.custom.GenericItemDecoration
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentUsersBinding
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.shared.components.BaseFragment
import io.capstone.ludendorff.features.user.editor.UserEditorFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserFragment: BaseFragment(), OnItemActionListener<User>, BaseFragment.CascadeMenuDelegate {
    private var _binding: FragmentUsersBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: UserViewModel by activityViewModels()
    private val coreViewModel: CoreViewModel by activityViewModels()
    private val userAdapter = UserAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.actionButton.transitionName = TRANSITION_NAME_ROOT

        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_users,
            iconRes = R.drawable.ic_hero_menu,
            onNavigationClicked = { getOverlappingPanelLayout().openStartPanel() },
            menuRes = R.menu.menu_main,
            onMenuOptionClicked = ::onMenuItemClicked
        )

        with(binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = userAdapter
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        coreViewModel.userData.observe(viewLifecycleOwner) {
            binding.actionButton.isVisible = it.hasPermission(User.PERMISSION_MANAGE_USERS)
                    || it.hasPermission(User.PERMISSION_ADMINISTRATIVE)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.action.collect {
                when(it) {
                    is Response.Error -> {
                        android.util.Log.e("DEBUG", it.throwable.toString())
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
                                DeshiException.Code.PRECONDITION_FAILED -> {
                                    showGenericError(it.action)
                                }
                                DeshiException.Code.UNPROCESSABLE_ENTITY -> {
                                    MaterialDialog(requireContext()).show {
                                        lifecycleOwner(viewLifecycleOwner)
                                        title(R.string.error_generic)
                                        message(R.string.error_email_already_exists)
                                        positiveButton()
                                    }
                                }
                                DeshiException.Code.GENERIC -> {
                                    showGenericError(it.action)
                                }
                            }
                        } else showGenericError(it.action)
                    }
                    is Response.Success -> {
                        when(it.data) {
                            Response.Action.CREATE ->
                                createSnackbar(R.string.feedback_user_created)
                            Response.Action.UPDATE ->
                                createSnackbar(R.string.feedback_user_updated)
                            Response.Action.REMOVE ->
                                createSnackbar(R.string.feedback_user_removed)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            userAdapter.loadStateFlow.collectLatest {
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
                        binding.permissionView.root.hide()
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
                                userAdapter.itemCount < 1) {
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
                            binding.emptyView.root.isVisible = userAdapter.itemCount < 1
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.users.collectLatest {
                userAdapter.submitData(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            controller?.navigate(R.id.to_navigation_editor_user, null, null,
                FragmentNavigatorExtras(it to TRANSITION_NAME_ROOT))
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            userAdapter.refresh()
        }
    }

    override fun onActionPerformed(
        data: User?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        if (action == OnItemActionListener.Action.SELECT) {
            container?.let {
                controller?.navigate(R.id.navigation_editor_user,
                    bundleOf(UserEditorFragment.EXTRA_USER to data), null,
                    FragmentNavigatorExtras(
                        it to TRANSITION_NAME_ROOT + data?.userId)
                )
            }
        }
    }

    override fun onMenuItemClicked(id: Int) {
        when(id) {
            R.id.action_menu -> getOverlappingPanelLayout().openEndPanel()
        }
    }

    private fun showGenericError(action: Response.Action?) {
        when(action) {
            Response.Action.CREATE ->
                createSnackbar(R.string.feedback_user_create_error)
            Response.Action.UPDATE ->
                createSnackbar(R.string.feedback_user_update_error)
            Response.Action.REMOVE ->
                createSnackbar(R.string.feedback_user_remove_error)
        }
    }

}