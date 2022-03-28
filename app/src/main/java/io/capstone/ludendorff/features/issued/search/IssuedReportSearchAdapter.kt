package io.capstone.ludendorff.features.issued.search

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemIssuedReportBinding
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePagedListAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class IssuedReportSearchAdapter(
    private val onItemActionListener: OnItemActionListener<IssuedReportSearch>
): BasePagedListAdapter<IssuedReportSearch, IssuedReportSearchAdapter.IssuedReportSearchViewHolder>
    (IssuedReportSearch.DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IssuedReportSearchViewHolder {
        val binding = LayoutItemIssuedReportBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return IssuedReportSearchViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: IssuedReportSearchViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class IssuedReportSearchViewHolder(itemView: View)
        : BaseViewHolder<IssuedReportSearch>(itemView) {
        private val binding = LayoutItemIssuedReportBinding.bind(itemView)

        override fun onBind(data: IssuedReportSearch?) {
            val style = ForegroundColorSpan(
                ContextCompat.getColor(binding.root.context,
                    R.color.brand_primary))

            binding.root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + data?.issuedReportId
            binding.overlineTextView.text = data?.highlightedSerialNumber?.toSpannedString(style)
            binding.headerTextView.text = data?.highlightedFundCluster?.toSpannedString(style)
            binding.informationTextView.text = data?.highlightedEntityName?.toSpannedString(style)

        }
    }
}