package io.capstone.ludendorff.features.request.sent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
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
import io.capstone.ludendorff.databinding.FragmentRequestSentBinding
import io.capstone.ludendorff.features.request.Request
import io.capstone.ludendorff.features.shared.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SentRequestFragment: BaseFragment(), OnItemActionListener<Request> {
    private var _binding: FragmentRequestSentBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val requestAdapter = SentRequestAdapter(this)
    private val viewModel: SentRequestViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    controller?.navigateUp()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestSentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.toolbar, arrayOf(binding.swipeRefreshLayout, binding.emptyView.root,
            binding.permissionView.root, binding.errorView.root, binding.shimmerFrameLayout))

        binding.swipeRefreshLayout.setColorRes(R.color.keeper_primary, R.color.keeper_surface)
        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_sent_requests,
            onNavigationClicked = { controller?.navigateUp() }
        )

        with(binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = requestAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()

        viewLifecycleOwner.lifecycleScope.launch {
            requestAdapter.loadStateFlow.collectLatest {
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
                            binding.emptyView.root.isVisible = requestAdapter.itemCount < 1
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.requests.collectLatest {
                requestAdapter.submitData(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.swipeRefreshLayout.setOnRefreshListener {
            requestAdapter.refresh()
        }
    }

    override fun onActionPerformed(
        data: Request?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        when(action) {
            OnItemActionListener.Action.SELECT -> {
                SentRequestViewerBottomSheet(childFragmentManager).show {
                    arguments = bundleOf(SentRequestViewerBottomSheet.EXTRA_REQUEST to data)
                }
            }
            OnItemActionListener.Action.DELETE -> TODO()
        }
    }
}