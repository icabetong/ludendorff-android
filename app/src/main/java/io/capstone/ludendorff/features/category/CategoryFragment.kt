package io.capstone.ludendorff.features.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.GenericItemDecoration
import io.capstone.ludendorff.components.custom.SwipeItemCallback
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.*
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentCategoryBinding
import io.capstone.ludendorff.features.category.editor.CategoryEditorFragment
import io.capstone.ludendorff.features.category.subcategory.SubcategoryEditorBottomSheet
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BaseSearchFragment
import io.capstone.ludendorff.features.user.User
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryFragment: BaseFragment(), FragmentResultListener, OnItemActionListener<Category>,
    BaseFragment.CascadeMenuDelegate {
    private var _binding: FragmentCategoryBinding? = null
    private var controller: NavController? = null
    private var mainController: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by activityViewModels()
    private val authViewModel: CoreViewModel by activityViewModels()
    private val categoryAdapter = CategoryAdapter(this)

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
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(
            view, binding.appBar.toolbar, arrayOf(binding.swipeRefreshLayout, binding.emptyView.root,
                binding.errorView.root, binding.permissionView.root, binding.shimmerFrameLayout),
            binding.actionButton
        )

        binding.actionButton.transitionName = TRANSITION_NAME_ROOT
        binding.swipeRefreshLayout.setColorRes(R.color.brand_primary, R.color.brand_surface)
        binding.appBar.searchPlaceholderView.transitionName = BaseSearchFragment.TRANSITION_SEARCH
        binding.appBar.toolbar.setup (
            titleRes = R.string.activity_categories,
            onNavigationClicked = { controller?.navigateUp() },
            menuRes = R.menu.menu_core_types,
            onMenuOptionClicked = ::onMenuItemClicked
        )

        registerForFragmentResult(
            arrayOf(
                SubcategoryEditorBottomSheet.REQUEST_KEY_CREATE,
                SubcategoryEditorBottomSheet.REQUEST_KEY_UPDATE
            ), this
        )

        val isTablet = requireContext().isTablet()
        with(binding.recyclerView) {
            if (!isTablet) addItemDecoration(GenericItemDecoration(context))
            layoutManager = if (isTablet) GridLayoutManager(context, 2)
                else LinearLayoutManager(context)
            adapter = categoryAdapter

            ItemTouchHelper(SwipeItemCallback(context, categoryAdapter))
                .attachToRecyclerView(this)
        }
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
        mainController = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

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
            categoryAdapter.loadStateFlow.collectLatest {
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
                                categoryAdapter.itemCount < 1) {
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
                            binding.emptyView.root.isVisible = categoryAdapter.itemCount < 1
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
                                    createSnackbar(R.string.feedback_category_create_error,
                                        binding.actionButton)
                                Response.Action.UPDATE ->
                                    createSnackbar(R.string.feedback_category_update_error,
                                        binding.actionButton)
                                Response.Action.REMOVE ->
                                    createSnackbar(R.string.feedback_category_remove_error,
                                        binding.actionButton)
                            }
                        }
                    }
                    is Response.Success -> {
                        when(it.data) {
                            Response.Action.CREATE ->
                                createSnackbar(R.string.feedback_category_created,
                                    binding.actionButton)
                            Response.Action.UPDATE ->
                                createSnackbar(R.string.feedback_category_updated,
                                    binding.actionButton)
                            Response.Action.REMOVE ->
                                createSnackbar(R.string.feedback_category_removed,
                                    binding.actionButton)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collectLatest {
                categoryAdapter.submitData(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            mainController?.navigate(R.id.navigation_editor_category, null, null,
                FragmentNavigatorExtras(it to TRANSITION_NAME_ROOT))
        }
        binding.appBar.searchPlaceholderView.setOnClickListener {
            mainController?.navigate(R.id.navigation_search_type, null, null,
                FragmentNavigatorExtras(it to BaseSearchFragment.TRANSITION_SEARCH))
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            categoryAdapter.refresh()
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            SubcategoryEditorBottomSheet.REQUEST_KEY_CREATE -> {
                result.getParcelable<Category>(SubcategoryEditorBottomSheet.EXTRA_SUBCATEGORY)?.let {
                    viewModel.create(it)
                    categoryAdapter.refresh()
                }
            }
            SubcategoryEditorBottomSheet.REQUEST_KEY_UPDATE -> {
                result.getParcelable<Category>(SubcategoryEditorBottomSheet.EXTRA_SUBCATEGORY)?.let {
                    viewModel.update(it)
                    categoryAdapter.refresh()
                }
            }
        }
    }

    override fun onActionPerformed(
        data: Category?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        when(action) {
            OnItemActionListener.Action.SELECT -> {
                container?.let {
                    mainController?.navigate(R.id.navigation_editor_category,
                        bundleOf(CategoryEditorFragment.EXTRA_CATEGORY to data), null,
                        FragmentNavigatorExtras(it to TRANSITION_NAME_ROOT + data?.categoryId))
                }
            }
            OnItemActionListener.Action.DELETE -> {
                data?.let { viewModel.remove(it) }
                categoryAdapter.refresh()
            }
        }
    }

    override fun onMenuItemClicked(id: Int) {
        when(id) {
            R.id.action_sort_name_ascending -> {
                viewModel.changeSortDirection(Query.Direction.ASCENDING)
                categoryAdapter.refresh()
            }
            R.id.action_sort_name_descending -> {
                viewModel.changeSortDirection(Query.Direction.DESCENDING)
                categoryAdapter.refresh()
            }
        }
    }
}