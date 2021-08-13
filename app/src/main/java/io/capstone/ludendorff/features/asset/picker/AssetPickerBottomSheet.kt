package io.capstone.ludendorff.features.asset.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.components.custom.GenericItemDecoration
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentPickerAssetBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.AssetAdapter
import io.capstone.ludendorff.features.shared.components.BaseBottomSheet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AssetPickerBottomSheet(manager: FragmentManager): BaseBottomSheet(manager),
    OnItemActionListener<Asset> {
    private var _binding: FragmentPickerAssetBinding? = null

    private val binding get() = _binding!!
    private val viewModel: AssetPickerViewModel by activityViewModels()
    private val assetAdapter = AssetAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickerAssetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = assetAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        viewLifecycleOwner.lifecycleScope.launch {
            assetAdapter.loadStateFlow.collectLatest {
                when (it.refresh) {
                    is LoadState.Loading -> {
                        binding.progressIndicator.show()
                        binding.recyclerView.hide()
                    }
                    is LoadState.Error -> {
                        binding.emptyView.root.hide()
                        binding.errorView.root.hide()
                        binding.progressIndicator.hide()
                        binding.recyclerView.hide()

                        val errorState = when {
                            it.prepend is LoadState.Error -> it.prepend as LoadState.Error
                            it.append is LoadState.Error -> it.append as LoadState.Error
                            it.refresh is LoadState.Error -> it.refresh as LoadState.Error
                            else -> null
                        }


                        errorState?.let { e ->

                            /**
                             *  Check if the error that have returned is
                             *  EmptySnapshotException, which is used if
                             *  QuerySnapshot is empty. Therefore, we
                             *  will check if the adapter is also empty
                             *  and show the user the empty state.
                             */
                            if (e.error is EmptySnapshotException &&
                                assetAdapter.itemCount < 1)
                                binding.emptyView.root.show()
                            else binding.errorView.root.show()
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
            viewModel.assets.collectLatest {
                assetAdapter.submitData(it)
            }
        }
    }

    override fun onActionPerformed(
        data: Asset?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        if (action == OnItemActionListener.Action.SELECT) {
            setFragmentResult(REQUEST_KEY_PICK,
                bundleOf(EXTRA_ASSET to data))
            this.dismiss()
        }
    }

    companion object {
        const val REQUEST_KEY_PICK = "request:pick:asset"
        const val EXTRA_ASSET = "extra:asset"
    }
}