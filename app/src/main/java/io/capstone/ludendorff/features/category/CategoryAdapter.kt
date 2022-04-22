package io.capstone.ludendorff.features.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.interfaces.SwipeableAdapter
import io.capstone.ludendorff.databinding.LayoutItemCategoryBinding
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePagingAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class CategoryAdapter(
    private val onItemActionListener: OnItemActionListener<Category>
): BasePagingAdapter<Category, CategoryAdapter.CategoryViewHolder>(Category.DIFF_CALLBACK), SwipeableAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = LayoutItemCategoryBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return CategoryViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onSwipe(position: Int, direction: Int) {
        onItemActionListener.onActionPerformed(
            getItem(position), OnItemActionListener.Action.DELETE, null
        )
    }

    inner class CategoryViewHolder(itemView: View): BaseViewHolder<Category>(itemView) {
        private val binding = LayoutItemCategoryBinding.bind(itemView)

        override fun onBind(data: Category?) {
            binding.root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + data?.categoryId
            binding.nameTextView.text = data?.categoryName
            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    it)
            }
        }
    }
}