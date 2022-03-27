package io.capstone.ludendorff.features.type.search

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemSearchBinding
import io.capstone.ludendorff.features.type.Type
import io.capstone.ludendorff.features.shared.BasePagedListAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class TypeSearchAdapter(
    private val onItemActionListener: OnItemActionListener<Type>
): BasePagedListAdapter<Type, TypeSearchAdapter.TypeSearchViewHolder>(Type.DIFF_CALLBACK)  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeSearchViewHolder {
        val binding = LayoutItemSearchBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return TypeSearchViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: TypeSearchViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class TypeSearchViewHolder(itemView: View): BaseViewHolder<Type>(itemView) {
        private val binding = LayoutItemSearchBinding.bind(itemView)

        override fun onBind(data: Type?) {
            with(binding) {
                val style = ForegroundColorSpan(
                    ContextCompat.getColor(root.context,
                    R.color.brand_primary))

                nameTextView.text = data?.highlightedName?.toSpannedString(style) ?: data?.typeName

                root.setOnClickListener {
                    onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                        binding.root)
                }
            }
        }
    }
}