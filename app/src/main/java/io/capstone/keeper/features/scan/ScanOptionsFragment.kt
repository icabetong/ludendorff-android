package io.capstone.keeper.features.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import coil.load
import coil.transform.CircleCropTransformation
import io.capstone.keeper.R
import io.capstone.keeper.components.extensions.hide
import io.capstone.keeper.components.extensions.show
import io.capstone.keeper.databinding.FragmentOptionsScanBinding
import io.capstone.keeper.features.core.backend.Response
import io.capstone.keeper.features.shared.components.BaseFragment

class ScanOptionsFragment: BaseFragment() {
    private var _binding: FragmentOptionsScanBinding? = null

    private val binding get() = _binding!!
    private val viewModel: ScanViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOptionsScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.emptyView.root.show()
    }

    override fun onStart() {
        super.onStart()

        viewModel.assignment.observe(viewLifecycleOwner) { response ->
            when(response) {
                is Response.Error -> {
                    binding.decodeErrorView.root.show()
                    binding.nestedScrollView.hide()
                    binding.progressIndicator.hide()
                }
                is Response.Success -> {
                    binding.decodeErrorView.root.hide()
                    binding.progressIndicator.hide()
                    binding.nestedScrollView.show()

                    response.data?.let {
                        binding.assetNameTextView.text = it.asset?.assetName
                        binding.categoryTextView.text = it.asset?.category?.categoryName
                        it.asset?.status?.let { status ->
                            binding.assetStatusTextView.setText(status.getStringRes())
                        }

                        it.user?.let { user ->
                            binding.profileImageView.load(user.imageUrl) {
                                placeholder(R.drawable.ic_hero_user)
                                error(R.drawable.ic_hero_user)
                                transformations(CircleCropTransformation())
                            }
                            binding.userNameTextView.text = user.name
                            binding.emailTextView.text = user.email
                        }
                    }
                }
            }
        }

        viewModel.asset.observe(viewLifecycleOwner) { response ->
            when(response) {
                is Response.Error -> {
                    binding.decodeErrorView.root.show()
                    binding.nestedScrollView.hide()
                    binding.progressIndicator.hide()
                }
                is Response.Success -> {
                    binding.decodeErrorView.root.hide()
                    binding.progressIndicator.hide()
                    binding.nestedScrollView.show()

                    binding.profileImageView.setImageResource(R.drawable.ic_hero_exclamation)
                    binding.userNameTextView.setText(R.string.error_assignment_not_exist_header)
                    binding.emailTextView.setText(R.string.error_assignment_not_exist_summary)

                    response.data.let {
                        binding.assetNameTextView.text = it.assetName
                        binding.categoryTextView.text = it.category?.categoryName

                        it.status?.let { status ->
                            binding.assetStatusTextView.setText(status.getStringRes())
                        }
                    }
                }
            }
        }

        viewModel.assetId.observe(viewLifecycleOwner) {
            binding.progressIndicator.isVisible = it != null
            binding.emptyView.root.isVisible = it == null
        }
    }
}