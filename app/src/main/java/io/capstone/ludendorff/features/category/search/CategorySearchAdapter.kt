package io.capstone.ludendorff.features.category.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemSearchBinding
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.shared.BasePagedListAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class CategorySearchAdapter(
    private val onItemActionListener: OnItemActionListener<Category>
): BasePagedListAdapter<Category, CategorySearchAdapter.AssetSearchViewHolder>(Category.DIFF_CALLBACK)  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetSearchViewHolder {
        val binding = LayoutItemSearchBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return AssetSearchViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AssetSearchViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class AssetSearchViewHolder(itemView: View): BaseViewHolder<Category>(itemView) {
        private val binding = LayoutItemSearchBinding.bind(itemView)

        override fun onBind(data: Category?) {
            with(binding) {
                nameTextView.text = data?.categoryName

                root.setOnClickListener {
                    onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                        binding.root)
                }
            }
        }
    }
}