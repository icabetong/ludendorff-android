package io.capstone.ludendorff.features.issued.item.picker

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
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.helper.android.searchbox.connectView
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.GenericItemDecoration
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentPickerIssuedItemBinding
import io.capstone.ludendorff.features.issued.IssuedReport
import io.capstone.ludendorff.features.issued.IssuedReportAdapter
import io.capstone.ludendorff.features.issued.item.IssuedItem
import io.capstone.ludendorff.features.issued.item.IssuedItemAdapter
import io.capstone.ludendorff.features.issued.search.IssuedReportSearchAdapter
import io.capstone.ludendorff.features.shared.BasePickerFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IssuedItemPickerFragment(manager: FragmentManager): BasePickerFragment(manager),
    OnItemActionListener<GroupedIssuedItem> {
    private var _binding: FragmentPickerIssuedItemBinding? = null

    private val binding get() = _binding!!
    private val viewModel: IssuedItemPickerViewModel by viewModels()
    private val issuedItemAdapter = IssuedItemPickerAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickerIssuedItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private var issuedReportActionListener = object: OnItemActionListener<IssuedReport> {
        override fun onActionPerformed(
            data: IssuedReport?,
            action: OnItemActionListener.Action,
            container: View?
        ) {
            if (action == OnItemActionListener.Action.SELECT) {
                viewModel.issuedReport = data
                binding.recyclerView.adapter = issuedItemAdapter
                binding.progressIndicator.show()
            }
        }

    }

    private val issuedAdapter = IssuedReportAdapter(issuedReportActionListener)
    private val issuedSearchAdapter = IssuedReportSearchAdapter(issuedReportActionListener)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.appBar.toolbar, R.string.title_inventory_select, R.menu.menu_picker, { this.dismiss() }) {}

        searchView?.setOnQueryTextFocusChangeListener { _, hasFocus ->
            viewModel.onSearchMode = hasFocus

            searchView?.isIconified = !hasFocus
            binding.recyclerView.adapter = if (hasFocus) issuedSearchAdapter else issuedAdapter
        }
        searchView?.setOnCloseListener {
            dismissSearch()
            viewModel.onSearchMode = false
            true
        }

        searchView?.let {
            connectionHandler += viewModel.searchBox.connectView(SearchBoxViewAppCompat(it))
        }
        with(binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = issuedAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        issuedSearchAdapter.addLoadStateListener { _, loadState ->
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

                    if (issuedSearchAdapter.itemCount > 0)
                        binding.recyclerView.show()
                    else binding.emptyView.root.show()
                }
                is LoadState.Error -> binding.errorView.root.hide()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            issuedAdapter.loadStateFlow.collectLatest {
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
                                issuedAdapter.itemCount < 1)
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
            viewModel.inventories.collectLatest {
                issuedAdapter.submitData(it)
            }
        }
        viewModel.searchResults.observe(viewLifecycleOwner) {
            issuedSearchAdapter.submitList(it)
        }
        viewModel.issuedItems.observe(viewLifecycleOwner) {
            issuedItemAdapter.submit(it)
            binding.progressIndicator.hide()
        }
    }

    override fun onActionPerformed(
        data: GroupedIssuedItem?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        if (action == OnItemActionListener.Action.SELECT) {
            setFragmentResult(REQUEST_KEY_PICK, bundleOf(EXTRA_ISSUED_ITEM to data))
            this.dismiss()
        }
    }

    companion object {
        const val REQUEST_KEY_PICK = "request:pick:issued:item"
        const val EXTRA_ISSUED_ITEM = "extra:issued:item"
    }
}