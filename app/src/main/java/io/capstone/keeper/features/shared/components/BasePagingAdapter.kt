package io.capstone.keeper.features.shared.components

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BasePagingAdapter<T: Any, VH: RecyclerView.ViewHolder>(callback: DiffUtil.ItemCallback<T>)
    : PagingDataAdapter<T, VH>(callback)