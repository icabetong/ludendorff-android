package io.capstone.ludendorff.features.stockcard.entry

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.toLocalDate
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.utils.DateTimeFormatter
import io.capstone.ludendorff.databinding.LayoutItemStockCardEntryBinding
import io.capstone.ludendorff.features.shared.BaseAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class StockCardEntryAdapter(
    private val onItemActionListener: OnItemActionListener<StockCardEntry>
): BaseAdapter<StockCardEntryAdapter.StockCardEntryViewHolder>() {

    private val items = mutableListOf<StockCardEntry>()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<StockCardEntry>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockCardEntryViewHolder {
        val binding = LayoutItemStockCardEntryBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return StockCardEntryViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: StockCardEntryViewHolder, position: Int) {
        holder.onBind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class StockCardEntryViewHolder(itemView: View): BaseViewHolder<StockCardEntry>(itemView) {
        private val binding = LayoutItemStockCardEntryBinding.bind(itemView)

        override fun onBind(data: StockCardEntry?) {
            val formatter = DateTimeFormatter.getDateFormatter(isShort = true, withYear = true)

            binding.titleTextView.text = String.format(binding.root.context
                .getString(R.string.concat_issued_data), data?.issueOffice, data?.issueQuantity)
            binding.bodyTextView.text = formatter.format(data?.date?.toLocalDate())
            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    null)
            }
            binding.setSourceButton.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.DELETE,
                    null)
            }
        }
    }
}