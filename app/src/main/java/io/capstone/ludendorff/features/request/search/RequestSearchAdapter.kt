package io.capstone.ludendorff.features.request.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemRequestBinding
import io.capstone.ludendorff.features.request.Request
import io.capstone.ludendorff.features.shared.BasePagedListAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder

class RequestSearchAdapter(
    private val onItemActionListener: OnItemActionListener<Request>
): BasePagedListAdapter<Request, RequestSearchAdapter.RequestSearchViewHolder>(Request.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestSearchViewHolder {
        val binding = LayoutItemRequestBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return RequestSearchViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: RequestSearchViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class RequestSearchViewHolder(itemView: View): BaseViewHolder<Request>(itemView) {
        private val binding = LayoutItemRequestBinding.bind(itemView)

        override fun onBind(data: Request?) {
            with(binding) {

            }
        }
    }
}