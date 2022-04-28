package io.capstone.ludendorff.features.issued.item.picker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemIssuedItemBinding
import io.capstone.ludendorff.features.shared.BaseAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class IssuedItemPickerAdapter(private val actionListener: OnItemActionListener<GroupedIssuedItem>)
    : BaseAdapter<IssuedItemPickerAdapter.IssuedItemViewHolder>() {

    private val items = mutableListOf<GroupedIssuedItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<GroupedIssuedItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssuedItemViewHolder {
        val binding = LayoutItemIssuedItemBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return IssuedItemViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: IssuedItemViewHolder, position: Int) {
        holder.onBind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class IssuedItemViewHolder(itemView: View): BaseViewHolder<GroupedIssuedItem>(itemView) {
        val binding = LayoutItemIssuedItemBinding.bind(itemView)

        override fun onBind(data: GroupedIssuedItem?) {
            binding.headerTextView.text = data?.stockNumber
            binding.informationTextView.text = if (data?.items?.get(0) != null) data.items[0].description else ""
            binding.root.setOnClickListener {
                actionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    null)
            }
        }
    }
}