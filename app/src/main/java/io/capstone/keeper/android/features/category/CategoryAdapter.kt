package io.capstone.keeper.android.features.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.capstone.keeper.android.databinding.LayoutItemCategoryBinding
import io.capstone.keeper.android.features.shared.components.BaseListAdapter

class CategoryAdapter(
    private val onItemActionListener: OnItemActionListener
): BaseListAdapter<Category, CategoryAdapter.CategoryViewHolder>(Category.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = LayoutItemCategoryBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return CategoryViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class CategoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val binding = LayoutItemCategoryBinding.bind(itemView)

        fun onBind(category: Category?) {
            binding.nameTextView.text = category?.categoryName
            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(category, Action.SELECT)
            }
        }
    }
}