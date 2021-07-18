package io.capstone.keeper.features.user.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.components.exceptions.EmptySnapshotException
import io.capstone.keeper.components.extensions.hide
import io.capstone.keeper.components.extensions.show
import io.capstone.keeper.databinding.FragmentPickerUserBinding
import io.capstone.keeper.features.shared.components.BaseBottomSheet
import io.capstone.keeper.features.shared.components.BasePagingAdapter
import io.capstone.keeper.features.user.UserAdapter
import io.capstone.keeper.features.user.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserPickerBottomSheet(manager: FragmentManager): BaseBottomSheet(manager),
    BasePagingAdapter.OnItemActionListenerDeprecated {
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

    override fun onDestroy() {
        super.onDestroy()
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
                    }
                    is LoadState.Error -> {
                        binding.progressIndicator.hide()
                        binding.recyclerView.hide()

                        val errorState = when {
                            it.prepend is LoadState.Error -> it.prepend as LoadState.Error
                            it.append is LoadState.Error -> it.append as LoadState.Error
                            it.refresh is LoadState.Error -> it.refresh as LoadState.Error
                            else -> null
                        }

                        errorState?.let { e ->
                            if (e.error is EmptySnapshotException &&
                                    userAdapter.itemCount < 1) {
                                binding.errorView.root.show()
                            } else {
                                binding.errorView.root.show()
                            }
                        }
                    }
                    is LoadState.NotLoading -> {
                        binding.progressIndicator.hide()
                        binding.recyclerView.show()
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

    override fun <T> onActionPerformed(t: T, action: BasePagingAdapter.Action) {

    }
}