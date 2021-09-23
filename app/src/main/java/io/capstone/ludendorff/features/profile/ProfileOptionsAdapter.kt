package io.capstone.ludendorff.features.profile

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.recyclerview.widget.RecyclerView
import io.capstone.ludendorff.databinding.LayoutItemProfileBinding
import io.capstone.ludendorff.features.shared.BaseViewHolder

class ProfileOptionsAdapter(
    activity: Activity?,
    @MenuRes private val menuId: Int,
    private val profileOptionListener: ProfileOptionListener
): RecyclerView.Adapter<ProfileOptionsAdapter.ProfileViewHolder>() {

    private var itemList = mutableListOf<ProfileOption>()

    init {
        val temp = PopupMenu(activity, null).menu
        activity?.menuInflater?.inflate(menuId, temp)

        for (i in 0 until temp.size()) {
            val item = temp.getItem(i)
            itemList.add(ProfileOption(item.itemId, item.icon, item.title.toString()))
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = LayoutItemProfileBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ProfileViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.onBind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ProfileViewHolder(itemView: View): BaseViewHolder<ProfileOption>(itemView) {
        val binding = LayoutItemProfileBinding.bind(itemView)

        override fun onBind(data: ProfileOption?) {
            data?.let {
                binding.iconView.setImageDrawable(it.icon)
                binding.titleView.text = it.title

                binding.root.setOnClickListener { _ ->
                    profileOptionListener.onProfileOptionSelected(it.id)
                }
            }
        }
    }

    data class ProfileOption(var id: Int, var icon: Drawable, var title: String)

    interface ProfileOptionListener {
        fun onProfileOptionSelected(id: Int)
    }
}