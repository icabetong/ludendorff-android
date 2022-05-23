package io.capstone.ludendorff.features.issued

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.extensions.toLocalDate
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.components.utils.DateTimeFormatter
import io.capstone.ludendorff.databinding.LayoutItemIssuedReportBinding
import io.capstone.ludendorff.features.issued.item.picker.IssuedItemPickerFragment
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePagingAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class IssuedReportAdapter(
    private val onItemActionListener: OnItemActionListener<IssuedReport>
): BasePagingAdapter<IssuedReport,
        IssuedReportAdapter.IssuedReportViewHolder>(IssuedReport.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssuedReportViewHolder {
        val binding = LayoutItemIssuedReportBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return IssuedReportViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: IssuedReportViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class IssuedReportViewHolder(itemView: View): BaseViewHolder<IssuedReport>(itemView) {
        private val binding = LayoutItemIssuedReportBinding.bind(itemView)
        private val userPreferences = UserPreferences(itemView.context)
        private val formatter = DateTimeFormatter.getDateFormatter(isShort = true, withYear = true)

        override fun onBind(data: IssuedReport?) {
            binding.root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + data?.issuedReportId
            binding.overlineTextView.text = when(userPreferences.dataIssuedOverline) {
                IssuedReport.FIELD_SERIAL_NUMBER -> data?.serialNumber
                IssuedReport.FIELD_FUND_CLUSTER -> data?.fundCluster
                IssuedReport.FIELD_DATE ->
                    formatter.format(data?.date?.toLocalDate())
                else -> data?.serialNumber
            }
            binding.headerTextView.text = when(userPreferences.dataIssuedHeader) {
                IssuedReport.FIELD_SERIAL_NUMBER -> data?.serialNumber
                IssuedReport.FIELD_FUND_CLUSTER -> data?.fundCluster
                IssuedReport.FIELD_DATE ->
                    formatter.format(data?.date?.toLocalDate())
                else -> data?.fundCluster
            }
            binding.informationTextView.text = when(userPreferences.dataIssuedSummary) {
                IssuedReport.FIELD_SERIAL_NUMBER -> data?.serialNumber
                IssuedReport.FIELD_FUND_CLUSTER -> data?.fundCluster
                IssuedReport.FIELD_DATE ->
                    formatter.format(data?.date?.toLocalDate())
                else -> formatter.format(data?.date?.toLocalDate())
            }

            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    it)
            }
        }
    }
}