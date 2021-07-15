package io.capstone.keeper.features.shared.components

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<VH: RecyclerView.ViewHolder>: RecyclerView.Adapter<VH>() {

    abstract class BaseViewHolder<T>(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun onBind(t: T)
    }
}