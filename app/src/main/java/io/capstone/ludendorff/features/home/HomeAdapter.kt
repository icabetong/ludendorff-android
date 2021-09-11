package io.capstone.ludendorff.features.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.databinding.LayoutItemHomeBinding
import io.capstone.ludendorff.features.assignment.Assignment
import io.capstone.ludendorff.features.shared.components.BasePagingAdapter
import io.capstone.ludendorff.features.shared.components.BaseViewHolder

class HomeAdapter: BasePagingAdapter<Assignment, HomeAdapter.HomeViewHolder>(Assignment.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = LayoutItemHomeBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return HomeViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class HomeViewHolder(itemView: View): BaseViewHolder<Assignment>(itemView) {
        private val binding = LayoutItemHomeBinding.bind(itemView)

        override fun onBind(data: Assignment?) {
            with(binding) {
                overlineTextView.text = data?.asset?.category?.categoryName
                headerTextView.text = data?.asset?.assetName
                informationTextView.text = data?.formatDateAssigned(root.context)
            }
        }
    }
}