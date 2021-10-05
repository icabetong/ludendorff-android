package io.capstone.ludendorff.features.department.search

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemDepartmentBinding
import io.capstone.ludendorff.features.department.Department
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePagedListAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class DepartmentSearchAdapter(
    private val onItemActionListener: OnItemActionListener<Department>
): BasePagedListAdapter<Department, DepartmentSearchAdapter.DepartmentSearchViewHolder>(Department.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentSearchViewHolder {
        val binding = LayoutItemDepartmentBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return DepartmentSearchViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: DepartmentSearchViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class DepartmentSearchViewHolder(itemView: View): BaseViewHolder<Department>(itemView) {
        private val binding = LayoutItemDepartmentBinding.bind(itemView)

        override fun onBind(data: Department?) {
            with(binding) {
                val style = ForegroundColorSpan(ContextCompat.getColor(root.context, R.color.brand_primary))

                root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + data?.departmentId
                titleTextView.text = data?.highlightedName?.toSpannedString(style) ?: data?.name
                bodyTextView.text = data?.highlightedManager?.toSpannedString(style) ?: data?.manager?.name

                root.setOnClickListener {
                    onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                        binding.root)
                }
            }
        }
    }
}