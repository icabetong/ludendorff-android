package io.capstone.keeper.features.category.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.components.custom.GenericItemDecoration
import io.capstone.keeper.databinding.FragmentPickerCategoryBinding
import io.capstone.keeper.features.category.Category
import io.capstone.keeper.features.category.CategoryAdapter
import io.capstone.keeper.features.category.CategoryViewModel
import io.capstone.keeper.features.shared.components.BaseBottomSheet
import io.capstone.keeper.features.shared.components.BasePagingAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryPickerBottomSheet(manager: FragmentManager): BaseBottomSheet(manager),
    BasePagingAdapter.OnItemActionListener {

    private var _binding: FragmentPickerCategoryBinding? = null

    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by viewModels()
    private val categoryAdapter = CategoryAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickerCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = categoryAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collectLatest {
                categoryAdapter.submitData(it)
            }
        }
    }

    override fun <T> onActionPerformed(t: T, action: BasePagingAdapter.Action) {
        if (t is Category) {
            when (action) {
                BasePagingAdapter.Action.SELECT -> {
                    setFragmentResult(REQUEST_KEY_PICK, bundleOf(EXTRA_CATEGORY to t))
                    this.dismiss()
                }
                BasePagingAdapter.Action.DELETE -> {
                    return
                }
            }
        }
    }

    companion object {
        const val REQUEST_KEY_PICK = "request:pick"
        const val EXTRA_CATEGORY = "extra:category"
    }
}