package io.capstone.ludendorff.features.specs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.databinding.LayoutItemSpecsReadOnlyBinding
import io.capstone.ludendorff.features.shared.BaseListAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class SpecsReadOnlyAdapter
    : BaseListAdapter<Pair<String, String>, SpecsReadOnlyAdapter.SpecsViewHolder>(SpecsAdapter.Companion) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecsViewHolder {
        val binding = LayoutItemSpecsReadOnlyBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return SpecsViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: SpecsViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class SpecsViewHolder(itemView: View): BaseViewHolder<Pair<String, String>>(itemView) {
        private val binding = LayoutItemSpecsReadOnlyBinding.bind(itemView)

        override fun onBind(data: Pair<String, String>?) {
            with(binding) {
                nameTextView.text = data?.first
                valueTextView.text = data?.second
            }
        }
    }
}