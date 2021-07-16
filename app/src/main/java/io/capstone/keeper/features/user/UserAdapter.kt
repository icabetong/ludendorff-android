package io.capstone.keeper.features.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.keeper.databinding.LayoutItemUserBinding
import io.capstone.keeper.features.shared.components.BasePagingAdapter
import io.capstone.keeper.features.shared.components.BaseViewHolder


class UserAdapter: BasePagingAdapter<User, UserAdapter.UserViewHolder>(User.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = LayoutItemUserBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return UserViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class UserViewHolder(itemView: View): BaseViewHolder<User>(itemView) {
        private val binding = LayoutItemUserBinding.bind(itemView)

        override fun onBind(data: User?) {
            binding.nameTextView.text = data?.getDisplayName()
            binding.emailTextView.text = data?.email
        }
    }
}