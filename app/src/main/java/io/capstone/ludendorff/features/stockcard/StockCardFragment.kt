package io.capstone.ludendorff.features.stockcard

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.GenericItemDecoration
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.*
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentStockCardBinding
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BaseSearchFragment
import io.capstone.ludendorff.features.stockcard.editor.StockCardEditorFragment
import io.capstone.ludendorff.features.user.User
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StockCardFragment: BaseFragment(), OnItemActionListener<StockCard>,
    BaseFragment.CascadeMenuDelegate {
    private var _binding: FragmentStockCardBinding? = null
    private var controller: NavController? = null
    private var mainController: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: StockCardViewModel by activityViewModels()
    private val authViewModel: CoreViewModel by activityViewModels()
    private val stockCardAdapter = StockCardAdapter(this)

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
        _binding = FragmentStockCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(binding.root, binding.appBar.toolbar, arrayOf(binding.swipeRefreshLayout,
            binding.emptyView.root, binding.errorView.root, binding.shimmerFrameLayout),
            binding.actionButton)

        binding.actionButton.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.searchPlaceholderView.transitionName = BaseSearchFragment.TRANSITION_SEARCH
        binding.swipeRefreshLayout.setColorRes(R.color.brand_primary, R.color.brand_surface)
        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_stock_card,
            iconRes = R.drawable.ic_round_menu_24,
            onNavigationClicked = { triggerNavigationDrawer() },
            menuRes = R.menu.menu_core_stock_card,
            onMenuOptionClicked = ::onMenuItemClicked
        )

        val isTablet = requireContext().isTablet()
        with(binding.recyclerView) {
            if (!isTablet) addItemDecoration(GenericItemDecoration(context))
            layoutManager = if (isTablet) GridLayoutManager(context, 2)
                else LinearLayoutManager(context)
            adapter = stockCardAdapter
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
        mainController = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        binding.informationCard.isVisible = viewModel.filterConstraint != null

        authViewModel.userData.observe(viewLifecycleOwner) {
            binding.actionButton.isVisible = it.hasPermission(User.PERMISSION_WRITE)
                    || it.hasPermission(User.PERMISSION_ADMINISTRATIVE)
        }

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
                                    createSnackbar(R.string.feedback_report_create_error,
                                        binding.actionButton)
                                Response.Action.UPDATE ->
                                    createSnackbar(R.string.feedback_report_update_error,
                                        binding.actionButton)
                                Response.Action.REMOVE ->
                                    createSnackbar(R.string.feedback_report_remove_error,
                                        binding.actionButton)
                            }
                        }
                    }
                    is Response.Success -> {
                        when(it.data) {
                            Response.Action.CREATE ->
                                createSnackbar(R.string.feedback_report_created,
                                    binding.actionButton)
                            Response.Action.UPDATE ->
                                createSnackbar(R.string.feedback_report_updated,
                                    binding.actionButton)
                            Response.Action.REMOVE ->
                                createSnackbar(R.string.feedback_report_removed,
                                    binding.actionButton)
                        }
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

        binding.actionButton.setOnClickListener {
            mainController?.navigate(R.id.navigation_editor_stock_card, null, null,
            FragmentNavigatorExtras(it to TRANSITION_NAME_ROOT))
        }
        binding.appBar.searchPlaceholderView.setOnClickListener {
            mainController?.navigate(R.id.navigation_search_stock_card, null, null,
                FragmentNavigatorExtras(it to BaseSearchFragment.TRANSITION_SEARCH))
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            stockCardAdapter.refresh()
        }
        binding.resetButton.setOnClickListener {
            viewModel.filterValue = null
            viewModel.filterConstraint = null
            viewModel.rebuildQuery()
            stockCardAdapter.refresh()

            binding.informationCard.isVisible = false
        }
    }

    override fun onActionPerformed(
        data: StockCard?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        when(action) {
            OnItemActionListener.Action.SELECT -> {
                container?.let {
                    mainController?.navigate(R.id.navigation_editor_stock_card,
                        bundleOf(StockCardEditorFragment.EXTRA_STOCK_CARD to data),
                        null,
                        FragmentNavigatorExtras(
                            it to TRANSITION_NAME_ROOT + data?.stockCardId)
                    )
                }
            }
            OnItemActionListener.Action.DELETE -> {
                MaterialDialog(requireContext()).show {
                    lifecycleOwner(viewLifecycleOwner)
                    title(R.string.dialog_remove_stock_card_title)
                    message(R.string.dialog_remove_stock_card_message)
                    positiveButton(R.string.button_remove) {
                        data?.let { item -> viewModel.remove(item) }
                    }
                    negativeButton(R.string.button_cancel)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun onMenuItemClicked(id: Int) {
        when(id) {
            R.id.action_sort_entity_name_ascending -> {
                viewModel.sortMethod = StockCard.FIELD_ENTITY_NAME
                viewModel.sortDirection = Query.Direction.ASCENDING
                viewModel.rebuildQuery()
                stockCardAdapter.refresh()
            }
            R.id.action_sort_entity_name_descending -> {
                viewModel.sortMethod = StockCard.FIELD_ENTITY_NAME
                viewModel.sortDirection = Query.Direction.DESCENDING
                viewModel.rebuildQuery()
                stockCardAdapter.refresh()
            }
            R.id.action_sort_asset_description_ascending -> {
                viewModel.sortMethod = StockCard.FIELD_DESCRIPTION
                viewModel.sortDirection = Query.Direction.ASCENDING
                viewModel.rebuildQuery()
                stockCardAdapter.refresh()
            }
            R.id.action_sort_asset_description_descending -> {
                viewModel.sortMethod = StockCard.FIELD_DESCRIPTION
                viewModel.sortDirection = Query.Direction.DESCENDING
                viewModel.rebuildQuery()
                stockCardAdapter.refresh()
            }
            R.id.action_sort_stock_number_ascending -> {
                viewModel.sortMethod = StockCard.FIELD_STOCK_NUMBER
                viewModel.sortDirection = Query.Direction.ASCENDING
                viewModel.rebuildQuery()
                stockCardAdapter.refresh()
            }
            R.id.action_sort_stock_number_descending -> {
                viewModel.sortMethod = StockCard.FIELD_STOCK_NUMBER
                viewModel.sortDirection = Query.Direction.DESCENDING
                viewModel.rebuildQuery()
                stockCardAdapter.refresh()
            }
            R.id.action_sort_measure_ascending -> {
                viewModel.sortMethod = StockCard.FIELD_UNIT_OF_MEASURE
                viewModel.sortDirection = Query.Direction.ASCENDING
                viewModel.rebuildQuery()
                stockCardAdapter.refresh()
            }
            R.id.action_sort_measure_descending -> {
                viewModel.sortMethod = StockCard.FIELD_UNIT_OF_MEASURE
                viewModel.sortDirection = Query.Direction.DESCENDING
                viewModel.rebuildQuery()
                stockCardAdapter.refresh()
            }
            R.id.action_filter_measure -> {
                MaterialDialog(requireContext()).show {
                    lifecycleOwner(viewLifecycleOwner)
                    title(R.string.dialog_unit_of_measure)
                    input(inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS) { _, text ->
                        viewModel.filterConstraint = StockCard.FIELD_UNIT_OF_MEASURE
                        viewModel.filterValue = text.toString()
                        viewModel.rebuildQuery()
                        stockCardAdapter.refresh()

                        binding.informationCard.isVisible = true
                        binding.informationCardText.text =
                            String.format(getString(R.string.info_dataset_filtered),
                                text, getString(R.string.hint_unit_of_measure))
                    }
                    positiveButton(R.string.button_continue)
                    negativeButton(R.string.button_cancel)
                }
            }
        }
    }
}