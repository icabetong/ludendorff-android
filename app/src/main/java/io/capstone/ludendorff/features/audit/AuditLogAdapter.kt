package io.capstone.ludendorff.features.audit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemAuditLogBinding
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePagingAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class AuditLogAdapter(
    private val onItemActionListener: OnItemActionListener<AuditLog>
): BasePagingAdapter<AuditLog, AuditLogAdapter.AuditLogsViewHolder>(AuditLog.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuditLogsViewHolder {
        val binding = LayoutItemAuditLogBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return AuditLogsViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AuditLogsViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class AuditLogsViewHolder(itemView: View): BaseViewHolder<AuditLog>(itemView) {
        private val binding = LayoutItemAuditLogBinding.bind(itemView)

        override fun onBind(data: AuditLog?) {
            binding.root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + data?.logEntryId
            binding.headerTextView.text = data?.user?.name
            binding.informationTextView.text = itemView.context.getString(R.string.concat_audit_log_summary,
                data?.dataType?.type, data?.operation?.operation, data?.identifier)

            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    null)
            }
        }
    }
}