package io.capstone.ludendorff.features.category.subcategory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemSubcategoryBinding
import io.capstone.ludendorff.features.shared.BaseAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class SubcategoryAdapter(private val actionListener: OnItemActionListener<String>): BaseAdapter<SubcategoryAdapter.SubcategoryViewHolder>() {
    private val items = mutableListOf<String>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(itemList: List<String>) {
        items.clear()
        items.addAll(itemList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubcategoryViewHolder {
        val binding = LayoutItemSubcategoryBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return SubcategoryViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: SubcategoryViewHolder, position: Int) {
        holder.onBind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class SubcategoryViewHolder(itemView: View): BaseViewHolder<String>(itemView) {
        private val binding = LayoutItemSubcategoryBinding.bind(itemView)

        override fun onBind(data: String?) {
            binding.nameTextView.text = data
            binding.removeButton.setOnClickListener {
                actionListener.onActionPerformed(data, OnItemActionListener.Action.DELETE, null)
            }
            binding.root.setOnClickListener {
                actionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT, null)
            }
        }
    }

}