package io.capstone.ludendorff.features.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.extensions.toLocalDate
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.components.utils.DateTimeFormatter
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
        private val userPreferences = UserPreferences(itemView.context)
        private val formatter = DateTimeFormatter.getDateFormatter(isShort = false, withYear = true)

        override fun onBind(data: InventoryReport?) {
            binding.root.transitionName =
                BaseFragment.TRANSITION_NAME_ROOT + data?.inventoryReportId
            binding.overlineTextView.text = when(userPreferences.dataInventoryOverline) {
                InventoryReport.FIELD_YEAR_MONTH -> data?.yearMonth
                InventoryReport.FIELD_ACCOUNTABILITY_DATE ->
                    formatter.format(data?.accountabilityDate?.toLocalDate())
                InventoryReport.FIELD_FUND_CLUSTER -> data?.fundCluster
                else -> data?.yearMonth
            }
            binding.headerTextView.text = when(userPreferences.dataInventoryHeader) {
                InventoryReport.FIELD_FUND_CLUSTER -> data?.fundCluster
                InventoryReport.FIELD_ACCOUNTABILITY_DATE ->
                    formatter.format(data?.accountabilityDate?.toLocalDate())
                else -> data?.fundCluster
            }
            binding.informationTextView.text = when(userPreferences.dataInventorySummary) {
                InventoryReport.FIELD_FUND_CLUSTER -> data?.fundCluster
                InventoryReport.FIELD_ACCOUNTABILITY_DATE ->
                    formatter.format(data?.accountabilityDate?.toLocalDate())
                else -> formatter.format(data?.accountabilityDate?.toLocalDate())
            }

            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    it)
            }
        }
    }
}