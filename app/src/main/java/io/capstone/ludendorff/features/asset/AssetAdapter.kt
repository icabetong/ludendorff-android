package io.capstone.ludendorff.features.asset

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemAssetBinding
import io.capstone.ludendorff.features.shared.components.BaseFragment
import io.capstone.ludendorff.features.shared.components.BasePagingAdapter
import io.capstone.ludendorff.features.shared.components.BaseViewHolder

class AssetAdapter(
    private val onItemActionListener: OnItemActionListener<Asset>
): BasePagingAdapter<Asset, AssetAdapter.AssetViewHolder>(Asset.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val binding = LayoutItemAssetBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return AssetViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class AssetViewHolder(itemView: View): BaseViewHolder<Asset>(itemView) {
        private val binding = LayoutItemAssetBinding.bind(itemView)

        override fun onBind(data: Asset?) {
            binding.root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + data?.assetId
            binding.headerTextView.text = data?.assetName
            binding.informationTextView.text = data?.category?.categoryName

            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT, it)
            }
        }
    }
}