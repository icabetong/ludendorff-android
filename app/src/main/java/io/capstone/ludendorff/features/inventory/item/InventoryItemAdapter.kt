package io.capstone.ludendorff.features.inventory.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.databinding.LayoutItemInventoryItemBinding
import io.capstone.ludendorff.features.shared.BaseAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class InventoryItemAdapter: BaseAdapter<InventoryItemAdapter.InventoryItemViewHolder>() {

    private val items = mutableListOf<InventoryItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<InventoryItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryItemViewHolder {
        val binding = LayoutItemInventoryItemBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return InventoryItemViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: InventoryItemViewHolder, position: Int) {
        holder.onBind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class InventoryItemViewHolder(itemView: View): BaseViewHolder<InventoryItem>(itemView) {
        private val binding = LayoutItemInventoryItemBinding.bind(itemView)

        override fun onBind(data: InventoryItem?) {
            binding.headerTextView.text = data?.description
            binding.informationTextView.text = data?.article
        }
    }

}