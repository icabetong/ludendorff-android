package io.capstone.ludendorff.features.stockcard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.databinding.LayoutItemStockCardBinding
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePagingAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class StockCardAdapter(private val onItemActionListener: OnItemActionListener<StockCard>):
    BasePagingAdapter<StockCard, StockCardAdapter.StockCardViewHolder>(StockCard.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockCardViewHolder {
        val binding = LayoutItemStockCardBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return StockCardViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: StockCardViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class StockCardViewHolder(itemView: View): BaseViewHolder<StockCard>(itemView) {
        private val binding = LayoutItemStockCardBinding.bind(itemView)
        private val userPreferences = UserPreferences(itemView.context)

        override fun onBind(data: StockCard?) {
            binding.root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + data?.stockCardId
            binding.overlineTextView.text = when(userPreferences.dataStockCardOverline) {
                StockCard.FIELD_STOCK_NUMBER -> data?.stockNumber
                StockCard.FIELD_ENTITY_NAME -> data?.entityName
                StockCard.FIELD_DESCRIPTION -> data?.description
                StockCard.FIELD_UNIT_OF_MEASURE -> data?.unitOfMeasure
                else -> data?.stockNumber
            }
            binding.headerTextView.text = when(userPreferences.dataStockCardHeader) {
                StockCard.FIELD_STOCK_NUMBER -> data?.stockNumber
                StockCard.FIELD_ENTITY_NAME -> data?.entityName
                StockCard.FIELD_DESCRIPTION -> data?.description
                StockCard.FIELD_UNIT_OF_MEASURE -> data?.unitOfMeasure
                else -> data?.description
            }
            binding.informationTextView.text = when(userPreferences.dataStockCardSummary) {
                StockCard.FIELD_STOCK_NUMBER -> data?.stockNumber
                StockCard.FIELD_ENTITY_NAME -> data?.entityName
                StockCard.FIELD_DESCRIPTION -> data?.description
                StockCard.FIELD_UNIT_OF_MEASURE -> data?.unitOfMeasure
                else -> data?.entityName
            }

            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    it)
            }
        }
    }
}