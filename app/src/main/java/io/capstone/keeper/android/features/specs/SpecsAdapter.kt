package io.capstone.keeper.android.features.specs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.capstone.keeper.android.databinding.LayoutItemSpecsBinding
import io.capstone.keeper.android.features.shared.components.BaseListAdapter

class SpecsAdapter
    : BaseListAdapter<Pair<String, String>, SpecsAdapter.SpecsViewHolder>
    (DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecsViewHolder {
        val binding = LayoutItemSpecsBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return SpecsViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: SpecsViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class SpecsViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {
        private val binding = LayoutItemSpecsBinding.bind(itemView)

        fun onBind(t: Pair<String, String>) {
            binding.nameTextView.text = t.first
            binding.valueTextView.text = t.second
        }
    }

    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Pair<String, String>>() {
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
}