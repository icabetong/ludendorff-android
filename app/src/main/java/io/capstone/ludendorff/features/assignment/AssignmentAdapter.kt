package io.capstone.ludendorff.features.assignment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemAssignmentBinding
import io.capstone.ludendorff.features.shared.components.BaseFragment
import io.capstone.ludendorff.features.shared.components.BasePagingAdapter
import io.capstone.ludendorff.features.shared.components.BaseViewHolder

class AssignmentAdapter(
    private val onItemActionListener: OnItemActionListener<Assignment>
) : BasePagingAdapter<Assignment, AssignmentAdapter.AssignmentViewHolder>(Assignment.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val binding = LayoutItemAssignmentBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return AssignmentViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class AssignmentViewHolder(itemView: View): BaseViewHolder<Assignment>(itemView) {
        private val binding = LayoutItemAssignmentBinding.bind(itemView)

        override fun onBind(data: Assignment?) {
            data?.let {
                binding.root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + it.assignmentId
                binding.overlineTextView.text = it.asset?.category?.categoryName
                binding.headerTextView.text = it.asset?.assetName
                binding.informationTextView.text = it.user?.name
            }

            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT, it)
            }
        }
    }
}