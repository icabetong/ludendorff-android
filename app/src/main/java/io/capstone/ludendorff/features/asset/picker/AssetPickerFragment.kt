package io.capstone.ludendorff.features.asset.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.helper.android.searchbox.connectView
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.GenericItemDecoration
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.isTablet
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.databinding.FragmentPickerAssetBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.AssetAdapter
import io.capstone.ludendorff.features.asset.search.AssetSearchAdapter
import io.capstone.ludendorff.features.shared.BasePickerFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AssetPickerFragment(manager: FragmentManager): BasePickerFragment(manager),
    OnItemActionListener<Asset> {
    private var _binding: FragmentPickerAssetBinding? = null

    private val binding get() = _binding!!
    private val viewModel: AssetPickerViewModel by viewModels()
    private val assetAdapter = AssetAdapter(this)
    private val assetSearchAdapter = AssetSearchAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickerAssetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.appBar.toolbar, R.string.title_asset_select, R.menu.menu_picker, { this.dismiss() }) {  }

        searchView?.setOnQueryTextFocusChangeListener { _, hasFocus ->
            viewModel.onSearchMode = hasFocus

            searchView?.isIconified = !hasFocus
            binding.recyclerView.adapter = if (hasFocus)
                assetSearchAdapter
            else assetAdapter
        }
        searchView?.setOnCloseListener {
            dismissSearch()
            viewModel.onSearchMode = false
            true
        }

        searchView?.let {
            connectionHandler += viewModel.searchBox.connectView(SearchBoxViewAppCompat(it))
        }

        val isTablet = requireContext().isTablet()
        with(binding.recyclerView) {
            if (!isTablet) addItemDecoration(GenericItemDecoration(context))
            layoutManager = if (isTablet) GridLayoutManager(context, 2)
                else LinearLayoutManager(context)
            adapter = assetAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        assetSearchAdapter.addLoadStateListener { _, loadState ->
            when(loadState) {
                LoadState.Loading -> {
                    binding.recyclerView.hide()
                    binding.emptyView.root.hide()
                    binding.progressIndicator.show()
                }
                is LoadState.NotLoading -> {
                    binding.recyclerView.show()
                    binding.emptyView.root.hide()
                    binding.progressIndicator.hide()

                    if (assetSearchAdapter.itemCount > 0)
                        binding.recyclerView.show()
                    else binding.emptyView.root.show()
                }
                is LoadState.Error -> binding.errorView.root.hide()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            assetAdapter.loadStateFlow.collectLatest {
                when (it.refresh) {
                    is LoadState.Loading -> {
                        binding.progressIndicator.show()
                        binding.recyclerView.hide()
                    }
                    is LoadState.Error -> {
                        val errorState = when {
                            it.prepend is LoadState.Error -> it.prepend as LoadState.Error
                            it.append is LoadState.Error -> it.append as LoadState.Error
                            it.refresh is LoadState.Error -> it.refresh as LoadState.Error
                            else -> null
                        }

                        errorState?.let { e ->
                            binding.emptyView.root.hide()
                            binding.errorView.root.hide()
                            binding.progressIndicator.hide()
                            binding.recyclerView.hide()

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
                        binding.errorView.root.hide()
                        binding.emptyView.root.hide()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.assets.collectLatest {
                assetAdapter.submitData(it)
            }
        }
        viewModel.searchResults.observe(viewLifecycleOwner) {
            assetSearchAdapter.submitList(it)
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