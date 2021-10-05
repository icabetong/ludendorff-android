package io.capstone.ludendorff.features.user.search

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.CoilProgressDrawable
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.LayoutItemUserBinding
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePagedListAdapter
import io.capstone.ludendorff.features.shared.BaseViewHolder
import io.capstone.ludendorff.features.user.User

class UserSearchAdapter(
    private val onItemActionListener: OnItemActionListener<User>
): BasePagedListAdapter<User, UserSearchAdapter.UserSearchViewHolder>(User.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSearchViewHolder {
        val binding = LayoutItemUserBinding.inflate(LayoutInflater.from(parent.context), parent,
            false)
        return UserSearchViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: UserSearchViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class UserSearchViewHolder(itemView: View): BaseViewHolder<User>(itemView) {
        private val binding = LayoutItemUserBinding.bind(itemView)

        override fun onBind(data: User?) {
            with(binding) {
                val style = ForegroundColorSpan(ContextCompat.getColor(root.context,
                    R.color.brand_primary))

                root.transitionName = BaseFragment.TRANSITION_NAME_ROOT + data?.userId
                overlineTextView.text = data?.highlightedPosition?.toSpannedString(style)
                    ?: data?.position
                headerTextView.text = String.format(root.context.getString(R.string.concat_user_name),
                    data?.highlightedFirstName?.toSpannedString(style) ?: data?.firstName,
                    data?.highlightedLastName?.toSpannedString(style) ?: data?.lastName)
                informationTextView.text = data?.highlightedEmail?.toSpannedString(style)
                    ?: data?.email
                if (data?.imageUrl != null)
                    binding.imageView.load(data.imageUrl) {
                        error(R.drawable.ic_flaticon_user)
                        scale(Scale.FILL)
                        transformations(CircleCropTransformation())
                        placeholder(
                            CoilProgressDrawable(binding.root.context,
                            R.color.brand_primary)
                        )
                    }
                else binding.imageView.setImageResource(R.drawable.ic_flaticon_user)

                root.setOnClickListener {
                    onItemActionListener.onActionPerformed(data, OnItemActionListener.Action.SELECT,
                        it)
                }
            }
        }
    }
}