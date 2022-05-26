package io.capstone.ludendorff.features.asset.usages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentAssetUsagesBinding
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.stockcard.StockCard
import io.capstone.ludendorff.features.stockcard.StockCardAdapter
import io.capstone.ludendorff.features.stockcard.editor.StockCardEditorFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FindUsagesFragment: BaseFragment(), OnItemActionListener<StockCard> {
    private var _binding: FragmentAssetUsagesBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val stockCardAdapter = StockCardAdapter(this)
    private val viewModel: FindUsagesViewModel by activityViewModels()

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
        _binding = FragmentAssetUsagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.toolbar)

        arguments?.getString(EXTRA_STOCK_NUMBER)?.let {
            viewModel.assetStockNumber = it
        }

        binding.appBar.toolbar.setup(
            titleRes = R.string.title_find_asset_stock_card_usages,
            onNavigationClicked = { controller?.navigateUp() }
        )
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = stockCardAdapter
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()

        /**
         *  Use Kotlin's coroutines to fetch the current loadState of
         *  the PagingAdapter; we will use the viewLifecycleOwner to
         *  avoid memory leaks as we are using fragments as the presenter.
         */
        viewLifecycleOwner.lifecycleScope.launch {
            stockCardAdapter.loadStateFlow.collectLatest {
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
                            binding.emptyView.root.isVisible = stockCardAdapter.itemCount < 1
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stockCards.collectLatest {
                stockCardAdapter.submitData(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.swipeRefreshLayout.setOnRefreshListener {
            stockCardAdapter.refresh()
        }
    }

    override fun onActionPerformed(
        data: StockCard?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        if (action == OnItemActionListener.Action.SELECT) {
            container?.let {
                controller?.navigate(R.id.navigation_editor_stock_card,
                    bundleOf(StockCardEditorFragment.EXTRA_STOCK_CARD to data), null,
                    FragmentNavigatorExtras(it to TRANSITION_NAME_ROOT + data?.stockCardId))
            }
        }
    }

    companion object {
        const val EXTRA_STOCK_NUMBER = "extra:stockNumber"
    }
}