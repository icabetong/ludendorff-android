package io.capstone.ludendorff.features.department

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.interfaces.SwipeableAdapter
import io.capstone.ludendorff.databinding.LayoutItemDepartmentBinding
import io.capstone.ludendorff.features.shared.components.BaseFragment
import io.capstone.ludendorff.features.shared.components.BasePagingAdapter
import io.capstone.ludendorff.features.shared.components.BaseViewHolder

class DepartmentAdapter(
    private val onItemActionListener: OnItemActionListener<Department>
): BasePagingAdapter<Department, DepartmentAdapter.DepartmentViewHolder>(Department.DIFF_CALLBACK),
    SwipeableAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentViewHolder {
        val binding = LayoutItemDepartmentBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return DepartmentViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: DepartmentViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onSwipe(position: Int, direction: Int) {
        onItemActionListener.onActionPerformed(getItem(position),
            OnItemActionListener.Action.DELETE, null)
    }

    inner class DepartmentViewHolder(itemView: View): BaseViewHolder<Department>(itemView) {
        private val binding = LayoutItemDepartmentBinding.bind(itemView)

        override fun onBind(data: Department?) {
            binding.root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + data?.departmentId
            binding.titleTextView.text = data?.name
            binding.bodyTextView.text = data?.manager?.name
                ?: binding.root.context.getString(R.string.hint_vacant)

            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    binding.root)
            }
        }
    }
}