package io.capstone.keeper.android.features.category

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
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.components.exceptions.EmptySnapshotException
import io.capstone.keeper.android.components.extensions.getCountThatFitsOnScreen
import io.capstone.keeper.android.components.extensions.onLastItemReached
import io.capstone.keeper.android.databinding.FragmentCategoryBinding
import io.capstone.keeper.android.features.category.editor.CategoryEditorBottomSheet
import io.capstone.keeper.android.features.shared.components.BaseFragment
import io.capstone.keeper.android.features.shared.components.BasePagingAdapter
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

        binding.actionButton.setOnClickListener {
            CategoryEditorBottomSheet(childFragmentManager).show()
        }

        with (binding.recyclerView) {
            onLastItemReached {  }
            adapter = categoryAdapter
        }

        registerForFragmentResult(
            arrayOf(
                CategoryEditorBottomSheet.REQUEST_KEY_CREATE,
                CategoryEditorBottomSheet.REQUEST_KEY_UPDATE
            ),
            this
        )

        binding.rowLayout.root.doOnLayout {
            val rowCount = it.getCountThatFitsOnScreen(it.context)
            binding.skeletonLayout.removeView(it)

            for (i in 0 until rowCount - 1) {
                val row = LayoutInflater.from(view.context)
                    .inflate(R.layout.layout_item_category_skeleton, binding.skeletonLayout, false) as ViewGroup
                binding.skeletonLayout.addView(row)
                row.requestLayout()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collectLatest {
                categoryAdapter.submitData(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            categoryAdapter.loadStateFlow.collectLatest {
                when (it.refresh) {
                    is LoadState.Loading -> {
                        binding.skeletonLayout.isVisible = true
                        binding.shimmerFrameLayout.isVisible = true
                        binding.shimmerFrameLayout.startShimmer()
                    }
                    is LoadState.NotLoading -> {
                        binding.skeletonLayout.isVisible = false
                        binding.shimmerFrameLayout.isVisible = false
                        binding.shimmerFrameLayout.stopShimmer()

                        if (it.refresh.endOfPaginationReached)
                            binding.emptyView.isVisible = categoryAdapter.itemCount < 1
                    }
                    is LoadState.Error -> {
                        binding.skeletonLayout.isVisible = false
                        binding.shimmerFrameLayout.isVisible = false

                        val errorState = when {
                            it.prepend is LoadState.Error -> it.prepend as LoadState.Error
                            it.append is LoadState.Error -> it.append as LoadState.Error
                            it.refresh is LoadState.Error -> it.refresh as LoadState.Error
                            else -> null
                        }
                        errorState?.let { e ->
                            if (e.error is EmptySnapshotException)
                                binding.emptyView.isVisible = categoryAdapter.itemCount < 1
                        }
                    }
                }
            }
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            CategoryEditorBottomSheet.REQUEST_KEY_CREATE -> {
                result.getParcelable<Category>(CategoryEditorBottomSheet.EXTRA_CATEGORY)?.let {
                    viewModel.create(it)
                }
            }
            CategoryEditorBottomSheet.REQUEST_KEY_UPDATE -> {
                result.getParcelable<Category>(CategoryEditorBottomSheet.EXTRA_CATEGORY)?.let {
                    viewModel.update(it)
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
                BasePagingAdapter.Action.DELETE -> TODO()
                BasePagingAdapter.Action.MODIFY -> TODO()
            }
        }
    }
}