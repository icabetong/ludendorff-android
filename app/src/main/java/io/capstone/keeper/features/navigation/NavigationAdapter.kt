package io.capstone.keeper.features.navigation

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.recyclerview.widget.RecyclerView
import io.capstone.keeper.databinding.LayoutItemNavigationBinding
import io.capstone.keeper.databinding.LayoutItemNavigationCurrentBinding

class NavigationAdapter(activity: Activity?,
                        @MenuRes private val id: Int,
                        @IdRes private var currentDestination: Int,
                        private val navigationItemListener: NavigationItemListener
) : RecyclerView.Adapter<NavigationAdapter.NavigationViewHolder>() {

    private var itemList = mutableListOf<NavigationItem>()

    init {
        val temp = PopupMenu(activity, null).menu
        activity?.menuInflater?.inflate(id, temp)

        for (i in 0 until temp.size()) {
            val item = temp.getItem(i)
            itemList.add(NavigationItem(item.itemId, item.icon, item.title.toString()))
        }
        notifyDataSetChanged()
    }

    fun setNewDestination(@IdRes destination: Int) {
        currentDestination = destination
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavigationViewHolder {
        return if (viewType == ITEM_TYPE_CURRENT_DESTINATION) {
            val binding = LayoutItemNavigationCurrentBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
            NavigationViewHolder(binding.root)
        } else {
            val binding = LayoutItemNavigationBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
            NavigationViewHolder(binding.root)
        }
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: NavigationViewHolder, position: Int) {
        if (holder.itemViewType == ITEM_TYPE_CURRENT_DESTINATION)
            holder.onBindCurrent(itemList[position])
        else holder.onBind(itemList[position])
    }

    override fun getItemViewType(position: Int): Int {
        return if (itemList[position].id == currentDestination)
            ITEM_TYPE_CURRENT_DESTINATION
        else ITEM_TYPE_AVAILABLE_DESTINATION
    }

    inner class NavigationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun onBind(item: NavigationItem) {
            with(LayoutItemNavigationBinding.bind(itemView)) {
                iconView.setImageDrawable(item.icon)
                titleView.text = item.title
                root.setOnClickListener { navigationItemListener.onItemSelected(item.id) }
            }
        }

        fun onBindCurrent(item: NavigationItem) {
            with(LayoutItemNavigationCurrentBinding.bind(itemView)) {
                iconView.setImageDrawable(item.icon)
                titleView.text = item.title
                root.setOnClickListener { navigationItemListener.onItemSelected(item.id) }
            }
        }
    }

    data class NavigationItem(var id: Int, var icon: Drawable?, var title: String)

    interface NavigationItemListener {
        fun onItemSelected(id: Int)
    }

    companion object {
        private const val ITEM_TYPE_AVAILABLE_DESTINATION = 0
        private const val ITEM_TYPE_CURRENT_DESTINATION = 1
    }
}