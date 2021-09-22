package io.capstone.ludendorff.features.request.sent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.LayoutItemRequestSentBinding
import io.capstone.ludendorff.features.request.Request
import io.capstone.ludendorff.features.shared.components.BasePagingAdapter
import io.capstone.ludendorff.features.shared.components.BaseViewHolder

class SentRequestAdapter: BasePagingAdapter<Request, SentRequestAdapter.SentRequestViewHolder>(Request.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SentRequestViewHolder {
        val binding = LayoutItemRequestSentBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return SentRequestViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: SentRequestViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class SentRequestViewHolder(itemView: View): BaseViewHolder<Request>(itemView) {
        private val binding = LayoutItemRequestSentBinding.bind(itemView)

        override fun onBind(data: Request?) {
            with(binding) {
                overlineTextView.text = data?.asset?.category?.categoryName
                headerTextView.text = data?.asset?.assetName

                with(informationTextView) {
                    text = if (data?.endorser != null)
                        String.format(context.getString(R.string.concat_endorsed_by),
                            data.endorser?.name)
                    else context.getString(R.string.info_not_endorsed)
                }
            }
        }
    }
}