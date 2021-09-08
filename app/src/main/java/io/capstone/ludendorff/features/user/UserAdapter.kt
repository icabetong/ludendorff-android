package io.capstone.ludendorff.features.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemUserBinding
import io.capstone.ludendorff.features.shared.components.BaseFragment
import io.capstone.ludendorff.features.shared.components.BasePagingAdapter
import io.capstone.ludendorff.features.shared.components.BaseViewHolder

class UserAdapter(
    private val onItemActionListener: OnItemActionListener<User>
): BasePagingAdapter<User, UserAdapter.UserViewHolder>(User.DIFF_CALLBACK) {

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
            data?.let {
                binding.root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + it.userId
                binding.headerTextView.text = it.getDisplayName()
                binding.informationTextView.text = it.email
                if (it.imageUrl != null)
                    binding.imageView.load(it.imageUrl) {
                        error(R.drawable.ic_flaticon_user)
                        placeholder(R.drawable.ic_flaticon_user)
                    }
                else binding.imageView.setImageResource(R.drawable.ic_flaticon_user)
            }

            binding.root.setOnClickListener {
                onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                    it)
            }
        }
    }
}