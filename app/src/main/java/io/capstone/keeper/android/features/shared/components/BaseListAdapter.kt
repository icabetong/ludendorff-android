package io.capstone.keeper.android.features.shared.components

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListAdapter<T, VH: RecyclerView.ViewHolder>(callback: DiffUtil.ItemCallback<T>)
    : ListAdapter<T, VH>(callback) {

    interface OnItemActionListener {
        fun <T> onActionPerformed(t: T, action: Action)
    }
    enum class Action { SELECT, DELETE, MODIFY }
}