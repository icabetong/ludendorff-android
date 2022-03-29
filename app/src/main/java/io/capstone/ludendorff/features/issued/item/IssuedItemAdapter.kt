package io.capstone.ludendorff.features.issued.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemIssuedItemBinding
import io.capstone.ludendorff.features.shared.BaseAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class IssuedItemAdapter(private val actionListener: OnItemActionListener<IssuedItem>)
    : BaseAdapter<IssuedItemAdapter.IssuedItemViewHolder>() {

    private val items = mutableListOf<IssuedItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<IssuedItem>) {
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

    inner class IssuedItemViewHolder(itemView: View): BaseViewHolder<IssuedItem>(itemView) {
        val binding = LayoutItemIssuedItemBinding.bind(itemView)

        override fun onBind(data: IssuedItem?) {
            binding.headerTextView.text = data?.description
            binding.informationTextView.text = data?.responsibilityCenter
            binding.root.setOnClickListener {
                actionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    null)
            }
        }
    }
}