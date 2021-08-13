package io.capstone.ludendorff.features.specs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemSpecsBinding
import io.capstone.ludendorff.features.shared.components.BaseListAdapter
import io.capstone.ludendorff.features.shared.components.BaseViewHolder

class SpecsAdapter(
    private val onItemActionListener: OnItemActionListener<Pair<String, String>>
): BaseListAdapter<Pair<String, String>, SpecsAdapter.SpecViewHolder>(Companion) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecViewHolder {
        val binding = LayoutItemSpecsBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return SpecViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: SpecViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class SpecViewHolder(itemView: View): BaseViewHolder<Pair<String, String>>(itemView) {
        private val binding = LayoutItemSpecsBinding.bind(itemView)

        override fun onBind(data: Pair<String, String>?) {
            data?.let {
                binding.nameTextView.text = it.first
                binding.valueTextView.text = it.second

                binding.root.setOnClickListener {
                    onItemActionListener.onActionPerformed(data,
                        OnItemActionListener.Action.SELECT, null)
                }
                binding.removeButton.setOnClickListener {
                    onItemActionListener.onActionPerformed(data,
                        OnItemActionListener.Action.DELETE, null)
                }
            }
        }
    }

    companion object: DiffUtil.ItemCallback<Pair<String, String>>() {
        override fun areItemsTheSame(
            oldItem: Pair<String, String>,
            newItem: Pair<String, String>
        ): Boolean {
            return oldItem.first == newItem.first
        }
        override fun areContentsTheSame(
            oldItem: Pair<String, String>,
            newItem: Pair<String, String>
        ): Boolean {
            return oldItem == newItem
        }
    }

}