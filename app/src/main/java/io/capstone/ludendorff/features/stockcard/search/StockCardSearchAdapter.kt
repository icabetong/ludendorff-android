package io.capstone.ludendorff.features.stockcard.search

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemStockCardBinding
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePagedListAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class StockCardSearchAdapter(
    private val onItemActionListener: OnItemActionListener<StockCardSearch>
): BasePagedListAdapter<StockCardSearch, StockCardSearchAdapter.StockCardSearchViewHolder>
    (StockCardSearch.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockCardSearchViewHolder {
        val binding = LayoutItemStockCardBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return StockCardSearchViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: StockCardSearchViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class StockCardSearchViewHolder(itemView: View): BaseViewHolder<StockCardSearch>(itemView) {
        private val binding = LayoutItemStockCardBinding.bind(itemView)

        override fun onBind(data: StockCardSearch?) {
            val style = ForegroundColorSpan(
                ContextCompat.getColor(binding.root.context,
                    R.color.brand_primary))

            binding.root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + data?.stockCardId
            binding.overlineTextView.text = data?.highlightableStockNumber?.toSpannedString(style)
            binding.headerTextView.text = data?.highlightableDescription?.toSpannedString(style)
            binding.informationTextView.text = data?.highlightableEntityName?.toSpannedString(style)
        }
    }
}