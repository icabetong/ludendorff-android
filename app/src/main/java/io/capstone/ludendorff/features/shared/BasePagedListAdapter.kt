package io.capstone.ludendorff.features.shared

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil

abstract class BasePagedListAdapter<T: Any, VH: BaseViewHolder<T>>(callback: DiffUtil.ItemCallback<T>)
    : PagedListAdapter<T, VH>(callback)