package io.capstone.ludendorff.features.assignment.search

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemAssignmentBinding
import io.capstone.ludendorff.features.assignment.Assignment
import io.capstone.ludendorff.features.shared.BasePagedListAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class AssignmentSearchAdapter (
    private val onItemActionListener: OnItemActionListener<Assignment>
): BasePagedListAdapter<Assignment, AssignmentSearchAdapter.AssignmentSearchViewHolder>(Assignment.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentSearchViewHolder {
        val binding = LayoutItemAssignmentBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return AssignmentSearchViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AssignmentSearchViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class AssignmentSearchViewHolder(itemView: View): BaseViewHolder<Assignment>(itemView) {
        private val binding = LayoutItemAssignmentBinding.bind(itemView)

        override fun onBind(data: Assignment?) {
            with(binding) {
                val style = ForegroundColorSpan(
                    ContextCompat.getColor(root.context,
                    R.color.brand_primary))

                overlineTextView.text = data?.highlightedCategoryName?.toSpannedString(style)
                    ?: data?.asset?.category?.categoryName
                headerTextView.text = data?.highlightedAssetName?.toSpannedString(style)
                    ?: data?.asset?.assetName
                informationTextView.text = data?.highlightedUserName?.toSpannedString(style)
                    ?: data?.user?.name

            }
        }
    }
}