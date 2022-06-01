package io.capstone.ludendorff.features.audit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.GenericItemDecoration
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.*
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentAuditLogsBinding
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BaseSearchFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuditLogsFragment: BaseFragment(), OnItemActionListener<AuditLog> {
    private var _binding: FragmentAuditLogsBinding? = null
    private var controller: NavController? = null
    private var mainController: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: AuditLogViewModel by activityViewModels()
    private val logsAdapter = AuditLogAdapter(this)

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
    ): View? {
        _binding = FragmentAuditLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(
            binding.root, binding.appBar.toolbar, arrayOf(binding.swipeRefreshLayout, binding.emptyView.root,
                binding.errorView.root, binding.permissionView.root, binding.shimmerFrameLayout)
        )

        binding.appBar.searchPlaceholderView.transitionName = BaseSearchFragment.TRANSITION_SEARCH
        binding.swipeRefreshLayout.setColorRes(R.color.brand_primary, R.color.brand_surface)
        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_audit_log,
            iconRes = R.drawable.ic_round_menu_24,
            onNavigationClicked = { triggerNavigationDrawer() },
        )

        val isTablet = requireContext().isTablet()
        with(binding.recyclerView) {
            if (!isTablet) addItemDecoration(GenericItemDecoration(context))
            layoutManager = if (isTablet) GridLayoutManager(context, 2)
            else LinearLayoutManager(context)
            adapter = logsAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
        mainController = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        /**
         *  Use Kotlin's coroutines to fetch the current loadState of
         *  the PagingAdapter; we will use the viewLifecycleOwner to
         *  avoid memory leaks as we are using fragments as the presenter.
         */
        viewLifecycleOwner.lifecycleScope.launch {
            logsAdapter.loadStateFlow.collectLatest {
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
                        binding.permissionView.root.hide()
                    }
                    /**
                     *  The PagingAdapter or any component related to fetch
                     *  the data have encountered an exception. Refer to the
                     *  CategoryPagingSource class to determine the logic
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
                            android.util.Log.e("DEBUG", e.toString())
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
                                logsAdapter.itemCount < 1) {
                                binding.emptyView.root.show()
                            } else if (e.error is FirebaseFirestoreException
                                && (e.error as FirebaseFirestoreException).code ==
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
                            binding.emptyView.root.isVisible = logsAdapter.itemCount < 1
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.auditLogs.collectLatest {
                logsAdapter.submitData(it)
            }
        }
    }

    override fun onActionPerformed(
        data: AuditLog?,
        action: OnItemActionListener.Action,
        container: View?
    ) {

    }
}