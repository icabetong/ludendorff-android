package io.capstone.keeper.android.features.user

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.capstone.keeper.android.databinding.LayoutItemUserBinding

class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val binding = LayoutItemUserBinding.bind(itemView)

    fun bind(user: User) {
        binding.nameTextView.text = user.getDisplayName()
        binding.emailTextView.text = user.email
    }
}