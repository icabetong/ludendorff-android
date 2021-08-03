package io.capstone.keeper.features.scan.image

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import coil.load
import io.capstone.keeper.components.interfaces.OnItemActionListener
import io.capstone.keeper.databinding.LayoutItemImageBinding
import io.capstone.keeper.features.shared.components.BaseListAdapter
import io.capstone.keeper.features.shared.components.BaseViewHolder

class ImageAdapter(
    private val onItemActionListener: OnItemActionListener<Uri>
): BaseListAdapter<Uri, ImageAdapter.ImageViewHolder>(Companion) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = LayoutItemImageBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ImageViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ImageViewHolder(itemView: View): BaseViewHolder<Uri>(itemView) {
        private val binding = LayoutItemImageBinding.bind(itemView)

        override fun onBind(data: Uri?) {
            binding.imageView.load(data)
            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    null)
            }
        }
    }

    companion object: DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }
}