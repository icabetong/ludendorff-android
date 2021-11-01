package io.capstone.ludendorff.features.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.interfaces.SwipeableAdapter
import io.capstone.ludendorff.databinding.LayoutItemNotificationBinding
import io.capstone.ludendorff.features.shared.BasePagingAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class NotificationAdapter (
    private val onItemActionListener: OnItemActionListener<Notification>
): BasePagingAdapter<Notification, NotificationAdapter.NotificationViewHolder>(Notification.DIFF_CALLBACK), SwipeableAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = LayoutItemNotificationBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return NotificationViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onSwipe(position: Int, direction: Int) {
        onItemActionListener.onActionPerformed(getItem(position), OnItemActionListener.Action.DELETE,
            null)
    }

    inner class NotificationViewHolder(itemView: View): BaseViewHolder<Notification>(itemView) {
        private val binding = LayoutItemNotificationBinding.bind(itemView)

        override fun onBind(data: Notification?) {
            with(binding) {
                val titleRes = root.context.resources.getIdentifier(data?.title, "string",
                    root.context.packageName)
                val bodyRes = root.context.resources.getIdentifier(data?.body, "string",
                    root.context.packageName)

                headerTextView.text = String.format(root.context.getString(titleRes),
                    data?.extras?.get(Notification.EXTRA_TARGET))
                informationTextView.text = String.format(root.context.getString(bodyRes),
                    data?.extras?.get(Notification.EXTRA_SENDER), data?.extras?.get(Notification.EXTRA_TARGET))
                metadataTextView.text = data?.formatTimestamp(root.context)
            }
        }
    }
}