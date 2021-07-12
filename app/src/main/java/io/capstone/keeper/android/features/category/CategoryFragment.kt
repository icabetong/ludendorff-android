package io.capstone.keeper.android.features.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.components.extensions.onLastItemReached
import io.capstone.keeper.android.databinding.FragmentCategoryBinding
import io.capstone.keeper.android.features.category.editor.CategoryEditorBottomSheet
import io.capstone.keeper.android.features.core.data.Response
import io.capstone.keeper.android.features.shared.components.BaseFragment
import io.capstone.keeper.android.features.shared.components.BaseListAdapter

@AndroidEntryPoint
class CategoryFragment: BaseFragment(), FragmentResultListener, BaseListAdapter.OnItemActionListener {
    private var _binding: FragmentCategoryBinding? = null
    private var controller: NavController? = null

    private val categoryAdapter = CategoryAdapter(this)
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by activityViewModels()

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
            onLastItemReached { viewModel.fetch() }
            adapter = categoryAdapter
        }

        registerForFragmentResult(
            arrayOf(
                CategoryEditorBottomSheet.REQUEST_KEY_CREATE,
                CategoryEditorBottomSheet.REQUEST_KEY_UPDATE
            ),
            this
        )
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            CategoryEditorBottomSheet.REQUEST_KEY_CREATE -> {
                result.getParcelable<Category>(CategoryEditorBottomSheet.EXTRA_CATEGORY)?.let {
                    viewModel.insert(it)
                }
            }
            CategoryEditorBottomSheet.REQUEST_KEY_UPDATE -> {
                result.getParcelable<Category>(CategoryEditorBottomSheet.EXTRA_CATEGORY)?.let {
                    viewModel.update(it)
                }
            }
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseListAdapter.Action) {
        if (t is Category) {
            when (action) {
                BaseListAdapter.Action.SELECT -> {
                    CategoryEditorBottomSheet(childFragmentManager).show {
                        arguments = bundleOf(CategoryEditorBottomSheet.EXTRA_CATEGORY to t)
                    }
                }
                BaseListAdapter.Action.DELETE -> TODO()
                BaseListAdapter.Action.MODIFY -> TODO()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.categories?.observe(viewLifecycleOwner) {
            when (it) {
                is Response.Success -> {
                    binding.progressIndicator.isVisible = false
                    if (it.value.isNotEmpty()) {
                        categoryAdapter.submitList(null)
                        categoryAdapter.submitList(it.value)
                        binding.emptyView.isVisible = false
                    } else binding.emptyView.isVisible = true
                }
                is Response.Error -> {
                    createSnackbar(R.string.error_generic)
                }
                is Response.InProgress -> {
                    binding.progressIndicator.isVisible = true
                }
            }
        }
    }
}