package io.capstone.ludendorff.features.user.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentPickerUserBinding
import io.capstone.ludendorff.features.shared.BaseBottomSheet
import io.capstone.ludendorff.features.user.User
import io.capstone.ludendorff.features.user.UserAdapter
import io.capstone.ludendorff.features.user.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserPickerBottomSheet(manager: FragmentManager): BaseBottomSheet(manager),
    OnItemActionListener<User> {
    private var _binding: FragmentPickerUserBinding? = null

    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels()
    private val userAdapter = UserAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickerUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.recyclerView) {
            adapter = userAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        viewLifecycleOwner.lifecycleScope.launch {
            userAdapter.loadStateFlow.collectLatest {
                when (it.refresh) {
                    is LoadState.Loading -> {
                        binding.progressIndicator.show()
                        binding.recyclerView.hide()
                        binding.errorView.root.hide()
                        binding.emptyView.root.hide()
                    }
                    is LoadState.Error -> {
                        binding.progressIndicator.hide()
                        binding.recyclerView.hide()

                        binding.errorView.root.hide()
                        binding.emptyView.root.hide()
                        val errorState = when {
                            it.prepend is LoadState.Error -> it.prepend as LoadState.Error
                            it.append is LoadState.Error -> it.append as LoadState.Error
                            it.refresh is LoadState.Error -> it.refresh as LoadState.Error
                            else -> null
                        }

                        errorState?.let { e ->
                            if (e.error is EmptySnapshotException &&
                                    userAdapter.itemCount < 1) {
                                binding.emptyView.root.show()
                            } else binding.errorView.root.show()
                        }
                    }
                    is LoadState.NotLoading -> {
                        binding.progressIndicator.hide()
                        binding.recyclerView.show()
                        binding.errorView.root.hide()
                        binding.emptyView.root.hide()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.users.collectLatest {
                userAdapter.submitData(it)
            }
        }
    }

    override fun onActionPerformed(
        data: User?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        when(action) {
            OnItemActionListener.Action.SELECT -> {
                setFragmentResult(REQUEST_KEY_PICK, bundleOf(EXTRA_USER to data))
                this.dismiss()
            }
            OnItemActionListener.Action.DELETE -> {}
        }
    }

    companion object {
        const val REQUEST_KEY_PICK = "request:pick:user"
        const val EXTRA_USER = "extra:user"
    }
}