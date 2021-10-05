package io.capstone.ludendorff.features.category.search

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemSearchBinding
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.shared.BasePagedListAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class CategorySearchAdapter(
    private val onItemActionListener: OnItemActionListener<Category>
): BasePagedListAdapter<Category, CategorySearchAdapter.CategorySearchViewHolder>(Category.DIFF_CALLBACK)  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategorySearchViewHolder {
        val binding = LayoutItemSearchBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return CategorySearchViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: CategorySearchViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class CategorySearchViewHolder(itemView: View): BaseViewHolder<Category>(itemView) {
        private val binding = LayoutItemSearchBinding.bind(itemView)

        override fun onBind(data: Category?) {
            with(binding) {
                val style = ForegroundColorSpan(
                    ContextCompat.getColor(root.context,
                    R.color.brand_primary))

                nameTextView.text = data?.highlightedName?.toSpannedString(style) ?: data?.categoryName

                root.setOnClickListener {
                    onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                        binding.root)
                }
            }
        }
    }
}