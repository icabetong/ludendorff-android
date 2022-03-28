package io.capstone.ludendorff.features.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemInventoryReportBinding
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePagingAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class InventoryReportAdapter(
    private val onItemActionListener: OnItemActionListener<InventoryReport>
): BasePagingAdapter<InventoryReport,
        InventoryReportAdapter.InventoryReportViewHolder>(InventoryReport.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryReportViewHolder {
        val binding = LayoutItemInventoryReportBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return InventoryReportViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: InventoryReportViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class InventoryReportViewHolder(itemView: View)
        : BaseViewHolder<InventoryReport>(itemView) {
        private val binding = LayoutItemInventoryReportBinding.bind(itemView)

        override fun onBind(data: InventoryReport?) {
            binding.root.transitionName =
                BaseFragment.TRANSITION_NAME_ROOT + data?.inventoryReportId
            binding.overlineTextView.text = data?.yearMonth
            binding.headerTextView.text = data?.fundCluster
            binding.informationTextView.text = data?.entityName

            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    it)
            }
        }
    }
}