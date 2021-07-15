package io.capstone.keeper.features.shared.components

import android.view.View
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BasePagingAdapter<T: Any, VH: RecyclerView.ViewHolder>(callback: DiffUtil.ItemCallback<T>)
    : PagingDataAdapter<T, VH>(callback) {

    interface OnItemActionListener {
        fun <T> onActionPerformed(t: T, action: Action)
    }
    enum class Action { SELECT, DELETE }
}