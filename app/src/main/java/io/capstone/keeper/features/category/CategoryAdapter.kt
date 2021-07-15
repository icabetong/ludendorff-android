package io.capstone.keeper.features.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.keeper.components.interfaces.SwipeableAdapter
import io.capstone.keeper.databinding.LayoutItemCategoryBinding
import io.capstone.keeper.features.shared.components.BasePagingAdapter
import io.capstone.keeper.features.shared.components.BaseViewHolder

class CategoryAdapter(
    private val onItemActionListener: OnItemActionListener
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
        onItemActionListener.onActionPerformed(getItem(position), Action.DELETE)
    }

    inner class CategoryViewHolder(itemView: View): BaseViewHolder<Category>(itemView) {
        private val binding = LayoutItemCategoryBinding.bind(itemView)

        override fun onBind(data: Category?) {
            binding.nameTextView.text = data?.categoryName
            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, Action.SELECT)
            }
        }
    }
}