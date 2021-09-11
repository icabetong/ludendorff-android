package io.capstone.ludendorff.features.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.databinding.LayoutItemNotificationBinding
import io.capstone.ludendorff.features.shared.components.BasePagingAdapter
import io.capstone.ludendorff.features.shared.components.BaseViewHolder

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
                headerTextView.text = data?.title
                informationTextView.text = data?.body
            }
        }
    }
}