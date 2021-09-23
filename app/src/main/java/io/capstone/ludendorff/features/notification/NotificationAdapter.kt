package io.capstone.ludendorff.features.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.databinding.LayoutItemNotificationBinding
import io.capstone.ludendorff.features.shared.BasePagingAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class NotificationAdapter
    : BasePagingAdapter<Notification, NotificationAdapter.NotificationViewHolder>(Notification.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = LayoutItemNotificationBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return NotificationViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class NotificationViewHolder(itemView: View): BaseViewHolder<Notification>(itemView) {
        private val binding = LayoutItemNotificationBinding.bind(itemView)

        override fun onBind(data: Notification?) {
            with(binding) {
                val titleRes = root.context.resources.getIdentifier(data?.title, "string",
                    root.context.packageName)
                val bodyRes = root.context.resources.getIdentifier(data?.body, "string",
                    root.context.packageName)

                headerTextView.setText(titleRes)
                informationTextView.setText(bodyRes)
                metadataTextView.text = data?.formatTimestamp(root.context)
            }
        }
    }
}