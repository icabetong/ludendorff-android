package io.capstone.ludendorff.features.request

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.interfaces.SwipeableAdapter
import io.capstone.ludendorff.databinding.LayoutItemRequestBinding
import io.capstone.ludendorff.features.shared.BasePagingAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class RequestAdapter(
    private val itemActionListener: OnItemActionListener<Request>
): BasePagingAdapter<Request, RequestAdapter.RequestViewHolder>(Request.DIFF_CALLBACK), SwipeableAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = LayoutItemRequestBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return RequestViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onSwipe(position: Int, direction: Int) {
        itemActionListener.onActionPerformed(getItem(position), OnItemActionListener.Action.DELETE,
            null)
    }

    inner class RequestViewHolder(itemView: View): BaseViewHolder<Request>(itemView) {
        private val binding = LayoutItemRequestBinding.bind(itemView)

        override fun onBind(data: Request?) {
            with(binding) {
                titleTextView.text = data?.petitioner?.name
                bodyTextView.text = data?.asset?.assetName

                root.setOnClickListener {
                    itemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                        null)
                }
            }
        }
    }
}