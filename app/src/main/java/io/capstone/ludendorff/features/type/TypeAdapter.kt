package io.capstone.ludendorff.features.type

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.interfaces.SwipeableAdapter
import io.capstone.ludendorff.databinding.LayoutItemTypeBinding
import io.capstone.ludendorff.features.shared.BasePagingAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class TypeAdapter(
    private val onItemActionListener: OnItemActionListener<Type>
): BasePagingAdapter<Type, TypeAdapter.CategoryViewHolder>(Type.DIFF_CALLBACK), SwipeableAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = LayoutItemTypeBinding.inflate(LayoutInflater.from(parent.context),
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

    inner class CategoryViewHolder(itemView: View): BaseViewHolder<Type>(itemView) {
        private val binding = LayoutItemTypeBinding.bind(itemView)

        override fun onBind(data: Type?) {
            binding.nameTextView.text = data?.typeName
            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    null)
            }
        }
    }
}