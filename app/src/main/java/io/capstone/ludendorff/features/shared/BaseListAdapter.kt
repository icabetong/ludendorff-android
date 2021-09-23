package io.capstone.ludendorff.features.shared

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListAdapter<T, VH: RecyclerView.ViewHolder>(callback: DiffUtil.ItemCallback<T>)
    : ListAdapter<T, VH>(callback) {
}