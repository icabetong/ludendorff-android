package io.capstone.ludendorff.features.asset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
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
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.GenericItemDecoration
import io.capstone.ludendorff.components.exceptions.DocumentExistsException
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.*
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentAssetsBinding
import io.capstone.ludendorff.features.asset.editor.AssetEditorFragment
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.category.picker.CategoryPickerBottomSheet
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BaseSearchFragment
import io.capstone.ludendorff.features.user.User
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AssetFragment: BaseFragment(), OnItemActionListener<Asset>, BaseFragment.CascadeMenuDelegate,
    FragmentResultListener {
    private var _binding: FragmentAssetsBinding? = null
    private var controller: NavController? = null
    private var mainController: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: AssetViewModel by activityViewModels()
    private val authViewModel: CoreViewModel by activityViewModels()
    private val assetAdapter = AssetAdapter(this)

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
        _binding = FragmentAssetsBinding.inflate(layoutInflater, container, false)
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
                binding.errorView.root, binding.permissionView.root, binding.shimmerFrameLayout),
            binding.actionButton
        )

        binding.actionButton.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.searchPlaceholderView.transitionName = BaseSearchFragment.TRANSITION_SEARCH
        binding.swipeRefreshLayout.setColorRes(R.color.brand_primary, R.color.brand_surface)
        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_assets,
            iconRes = R.drawable.ic_round_menu_24,
            onNavigationClicked = { triggerNavigationDrawer() },
            menuRes = R.menu.menu_core_assets,
            onMenuOptionClicked = ::onMenuItemClicked
        )

        val isTablet = requireContext().isTablet()
        with(binding.recyclerView) {
            if (!isTablet) addItemDecoration(GenericItemDecoration(context))
            layoutManager = if (isTablet) GridLayoutManager(context, 2)
                else LinearLayoutManager(context)
            adapter = assetAdapter
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        registerForFragmentResult(arrayOf(
            CategoryPickerBottomSheet.REQUEST_KEY_PICK
        ), this)
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
            assetAdapter.loadStateFlow.collectLatest {
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
                            binding.emptyView.root.isVisible = assetAdapter.itemCount < 1
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
                        } else if (it.throwable is DocumentExistsException &&
                                it.action == Response.Action.CREATE) {
                            createSnackbar(R.string.feedback_stock_number_already_exists,
                                binding.actionButton)

                        } else {
                            when(it.action) {
                                Response.Action.CREATE ->
                                    createSnackbar(R.string.feedback_asset_create_error,
                                        binding.actionButton)
                                Response.Action.UPDATE ->
                                    createSnackbar(R.string.feedback_asset_update_error,
                                        binding.actionButton)
                                Response.Action.REMOVE ->
                                    createSnackbar(R.string.feedback_asset_remove_error,
                                        binding.actionButton)
                            }
                        }
                    }
                    is Response.Success -> {
                        when(it.data) {
                            Response.Action.CREATE ->
                                createSnackbar(R.string.feedback_asset_created,
                                    binding.actionButton)
                            Response.Action.UPDATE ->
                                createSnackbar(R.string.feedback_asset_updated,
                                    binding.actionButton)
                            Response.Action.REMOVE ->
                                createSnackbar(R.string.feedback_asset_removed,
                                    binding.actionButton)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.assets.collectLatest {
                assetAdapter.submitData(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            mainController?.navigate(R.id.navigation_editor_asset, null, null,
                FragmentNavigatorExtras(it to TRANSITION_NAME_ROOT))
        }
        binding.appBar.searchPlaceholderView.setOnClickListener {
            mainController?.navigate(R.id.navigation_search_asset, null, null,
                FragmentNavigatorExtras(it to BaseSearchFragment.TRANSITION_SEARCH))
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            assetAdapter.refresh()
        }
        binding.resetButton.setOnClickListener {
            viewModel.filterValue = null
            viewModel.filterConstraint = null
            viewModel.rebuildQuery()
            assetAdapter.refresh()

            binding.informationCard.isVisible = false
        }
    }

    override fun onActionPerformed(
        data: Asset?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        when(action) {
            OnItemActionListener.Action.SELECT -> {
                container?.let {
                    mainController?.navigate(R.id.navigation_editor_asset,
                        bundleOf(AssetEditorFragment.EXTRA_ASSET to data), null,
                        FragmentNavigatorExtras(
                            it to TRANSITION_NAME_ROOT + data?.stockNumber)
                    )
                }
            }
            OnItemActionListener.Action.DELETE -> {
                MaterialDialog(requireContext()).show {
                    lifecycleOwner(viewLifecycleOwner)
                    title(R.string.dialog_remove_asset_title)
                    message(R.string.dialog_remove_asset_message)
                    positiveButton(R.string.button_remove) {
                        data?.let { viewModel.remove(it) }
                    }
                    negativeButton(R.string.button_cancel)
                }
            }
        }
    }

    override fun onMenuItemClicked(id: Int) {
        when(id) {
            R.id.action_type -> {
                mainController?.navigate(R.id.navigation_type)
            }
            R.id.action_sort_asset_description_ascending -> {
                viewModel.sortMethod = Asset.FIELD_DESCRIPTION
                viewModel.sortDirection = Query.Direction.ASCENDING
                viewModel.rebuildQuery()
                assetAdapter.refresh()
            }
            R.id.action_sort_asset_description_descending -> {
                viewModel.sortMethod = Asset.FIELD_DESCRIPTION
                viewModel.sortDirection = Query.Direction.DESCENDING
                viewModel.rebuildQuery()
                assetAdapter.refresh()
            }
            R.id.action_sort_stock_number_ascending -> {
                viewModel.sortMethod = Asset.FIELD_STOCK_NUMBER
                viewModel.sortDirection = Query.Direction.ASCENDING
                viewModel.rebuildQuery()
                assetAdapter.refresh()
            }
            R.id.action_sort_stock_number_descending -> {
                viewModel.sortMethod = Asset.FIELD_STOCK_NUMBER
                viewModel.sortDirection = Query.Direction.DESCENDING
                viewModel.rebuildQuery()
                assetAdapter.refresh()
            }
            R.id.action_sort_type_ascending -> {
                viewModel.sortMethod = Asset.FIELD_CATEGORY_NAME
                viewModel.sortDirection = Query.Direction.ASCENDING
                viewModel.rebuildQuery()
                assetAdapter.refresh()
            }
            R.id.action_sort_type_descending -> {
                viewModel.sortMethod = Asset.FIELD_CATEGORY_NAME
                viewModel.sortDirection = Query.Direction.DESCENDING
                viewModel.rebuildQuery()
                assetAdapter.refresh()
            }
            R.id.action_filter_type -> {
                viewModel.filterConstraint = Asset.FIELD_CATEGORY_ID
                CategoryPickerBottomSheet(childFragmentManager)
                    .show()
            }
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            CategoryPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Category>(CategoryPickerBottomSheet.EXTRA_CATEGORY)?.let {
                    viewModel.filterValue = it.categoryId
                    viewModel.rebuildQuery()
                    assetAdapter.refresh()

                    binding.informationCard.isVisible = true
                    binding.informationCardText.text =
                        String.format(getString(R.string.info_dataset_filtered),
                            it.categoryName, getString(R.string.hint_category))
                }
            }
        }
    }
}