package io.capstone.keeper.features.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.paging.LoadState
import androidx.recyclerview.widget.ItemTouchHelper
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.components.custom.GenericItemDecoration
import io.capstone.keeper.components.custom.SwipeItemCallback
import io.capstone.keeper.components.exceptions.EmptySnapshotException
import io.capstone.keeper.components.extensions.getCountThatFitsOnScreen
import io.capstone.keeper.databinding.FragmentCategoryBinding
import io.capstone.keeper.features.category.editor.CategoryEditorBottomSheet
import io.capstone.keeper.features.shared.components.BaseFragment
import io.capstone.keeper.features.shared.components.BasePagingAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryFragment: BaseFragment(), FragmentResultListener, BasePagingAdapter.OnItemActionListener {
    private var _binding: FragmentCategoryBinding? = null
    private var controller: NavController? = null

    private var categoryAdapter = CategoryAdapter(this)
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
        setupToolbar(binding.appBar.toolbar, {
            controller?.navigateUp()
        }, R.string.activity_categories, R.drawable.ic_hero_x)

        registerForFragmentResult(
            arrayOf(
                CategoryEditorBottomSheet.REQUEST_KEY_CREATE,
                CategoryEditorBottomSheet.REQUEST_KEY_UPDATE
            ),
            this
        )
    }

    override fun onStart() {
        super.onStart()

        with (binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = categoryAdapter

            ItemTouchHelper(SwipeItemCallback(requireContext(), categoryAdapter))
                .attachToRecyclerView(this)
        }

        binding.rowLayout.root.doOnLayout {
            val rowCount = it.getCountThatFitsOnScreen(it.context)
            binding.skeletonLayout.removeView(it)

            for (i in 0 until rowCount - 1) {
                val row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.layout_item_category_skeleton, binding.skeletonLayout, false) as ViewGroup
                binding.skeletonLayout.addView(row)
                row.requestLayout()
            }
        }

        binding.actionButton.setOnClickListener {
            CategoryEditorBottomSheet(childFragmentManager).show()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            categoryAdapter.refresh()
        }
    }

    override fun onResume() {
        super.onResume()

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
                        binding.recyclerView.isVisible = false
                        binding.skeletonLayout.isVisible = true
                        binding.shimmerFrameLayout.isVisible = true
                        binding.shimmerFrameLayout.startShimmer()

                        binding.errorView.isVisible = false
                        binding.emptyView.isVisible = false
                    }
                    /**
                     *  The PagingAdapter or any component related to fetch
                     *  the data have encountered an exception. Refer to the
                     *  CategoryPagingSource class to determine the logic
                     *  used in handling different types of errors.
                     */
                    is LoadState.Error -> {
                        binding.recyclerView.isVisible = false
                        binding.skeletonLayout.isVisible = false
                        binding.shimmerFrameLayout.isVisible = false

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
                            if (e.error is EmptySnapshotException)
                                binding.emptyView.isVisible = categoryAdapter.itemCount < 1
                            else
                                binding.errorView.isVisible = true
                        }
                    }
                    is LoadState.NotLoading -> {
                        binding.recyclerView.isVisible = true
                        binding.skeletonLayout.isVisible = false
                        binding.shimmerFrameLayout.isVisible = false
                        binding.shimmerFrameLayout.stopShimmer()

                        binding.errorView.isVisible = false
                        binding.emptyView.isVisible = false

                        if (it.refresh.endOfPaginationReached)
                            binding.emptyView.isVisible = categoryAdapter.itemCount < 1
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

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            CategoryEditorBottomSheet.REQUEST_KEY_CREATE -> {
                result.getParcelable<Category>(CategoryEditorBottomSheet.EXTRA_CATEGORY)?.let {
                    viewModel.create(it)
                    categoryAdapter.refresh()
                }
            }
            CategoryEditorBottomSheet.REQUEST_KEY_UPDATE -> {
                result.getParcelable<Category>(CategoryEditorBottomSheet.EXTRA_CATEGORY)?.let {
                    viewModel.update(it)
                    categoryAdapter.refresh()
                }
            }
        }
    }

    override fun <T> onActionPerformed(t: T, action: BasePagingAdapter.Action) {
        if (t is Category) {
            when (action) {
                BasePagingAdapter.Action.SELECT -> {
                    CategoryEditorBottomSheet(childFragmentManager).show {
                        arguments = bundleOf(CategoryEditorBottomSheet.EXTRA_CATEGORY to t)
                    }
                }
                BasePagingAdapter.Action.DELETE -> {
                    viewModel.remove(t.categoryId)
                }
            }
        }
    }
}