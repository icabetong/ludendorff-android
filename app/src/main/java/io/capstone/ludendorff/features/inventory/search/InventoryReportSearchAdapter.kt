package io.capstone.ludendorff.features.inventory.search

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemInventoryReportBinding
import io.capstone.ludendorff.features.inventory.InventoryReport
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePagedListAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class InventoryReportSearchAdapter(
    private val onItemActionListener: OnItemActionListener<InventoryReportSearch>
): BasePagedListAdapter<InventoryReportSearch, InventoryReportSearchAdapter.InventoryReportSearchViewHolder>
    (InventoryReportSearch.DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InventoryReportSearchAdapter.InventoryReportSearchViewHolder {
        val binding = LayoutItemInventoryReportBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return InventoryReportSearchViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: InventoryReportSearchViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class InventoryReportSearchViewHolder(itemView: View)
        : BaseViewHolder<InventoryReportSearch>(itemView) {
        private val binding = LayoutItemInventoryReportBinding.bind(itemView)

        override fun onBind(data: InventoryReportSearch?) {
            val style = ForegroundColorSpan(
                ContextCompat.getColor(binding.root.context,
                    R.color.brand_primary))

            binding.root.transitionName =
                BaseFragment.TRANSITION_NAME_ROOT + data?.inventoryReportId
            binding.overlineTextView.text = data?.yearMonth
            binding.headerTextView.text = data?.highlightedFundCluster?.toSpannedString(style)
            binding.informationTextView.text = data?.highlightedEntityName?.toSpannedString(style)

            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    it)
            }
        }
    }
}