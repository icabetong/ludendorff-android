package io.capstone.ludendorff.features.asset.search

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemAssetBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePagedListAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class AssetSearchAdapter(
    private val onItemActionListener: OnItemActionListener<Asset>
): BasePagedListAdapter<AssetSearch, AssetSearchAdapter.AssetSearchViewHolder>(AssetSearch.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetSearchViewHolder {
        val binding = LayoutItemAssetBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return AssetSearchViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AssetSearchViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class AssetSearchViewHolder(itemView: View): BaseViewHolder<AssetSearch>(itemView) {
        private val binding = LayoutItemAssetBinding.bind(itemView)

        override fun onBind(data: AssetSearch?) {
            with(binding) {
                val style = ForegroundColorSpan(
                    ContextCompat.getColor(root.context,
                    R.color.brand_primary))

                root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + data?.stockNumber
                headerTextView.text = data?.highlightedName?.toSpannedString(style)
                    ?: data?.description
                informationTextView.text = data?.highlightedCategory?.toSpannedString(style)
                    ?: data?.classification

                root.setOnClickListener {
                    onItemActionListener.onActionPerformed(data?.toAsset(),
                        OnItemActionListener.Action.SELECT, binding.root)
                }
            }
        }
    }
}