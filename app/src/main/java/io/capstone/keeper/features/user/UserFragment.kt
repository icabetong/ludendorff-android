package io.capstone.keeper.features.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
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
import io.capstone.keeper.R
import io.capstone.keeper.components.custom.GenericItemDecoration
import io.capstone.keeper.components.exceptions.EmptySnapshotException
import io.capstone.keeper.components.extensions.getCountThatFitsOnScreen
import io.capstone.keeper.components.extensions.hide
import io.capstone.keeper.components.extensions.setup
import io.capstone.keeper.components.extensions.show
import io.capstone.keeper.components.interfaces.OnItemActionListener
import io.capstone.keeper.databinding.FragmentUsersBinding
import io.capstone.keeper.features.core.backend.Response
import io.capstone.keeper.features.shared.components.BaseFragment
import io.capstone.keeper.features.user.editor.UserEditorFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserFragment: BaseFragment(), OnItemActionListener<User> {
    private var _binding: FragmentUsersBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: UserViewModel by activityViewModels()
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
            onMenuOptionClicked = {
                when(it) {
                    R.id.action_menu -> getOverlappingPanelLayout().openEndPanel()
                }
            }
        )

        with(binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = userAdapter
        }

        binding.rowLayout.root.doOnLayout {
            val rowCount = it.getCountThatFitsOnScreen(it.context)
            binding.skeletonLayout.removeView(it)

            for (i in 0 until rowCount) {
                val row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.layout_item_user_skeleton,
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
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

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
                                    createSnackbar(R.string.feedback_user_create_error)
                                Response.Action.UPDATE ->
                                    createSnackbar(R.string.feedback_user_update_error)
                                else -> {}
                            }
                        }
                    }
                    is Response.Success -> {
                        when(it.data) {
                            Response.Action.CREATE ->
                                createSnackbar(R.string.feedback_user_created)
                            Response.Action.UPDATE ->
                                createSnackbar(R.string.feedback_user_updated)
                            else -> {}
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

}