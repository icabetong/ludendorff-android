package io.capstone.ludendorff.features.inventory.picker

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
import io.capstone.ludendorff.databinding.FragmentPickerInventoryBinding
import io.capstone.ludendorff.features.inventory.InventoryReport
import io.capstone.ludendorff.features.inventory.InventoryReportAdapter
import io.capstone.ludendorff.features.inventory.search.InventoryReportSearchAdapter
import io.capstone.ludendorff.features.shared.BasePickerFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventoryReportPickerFragment(manager: FragmentManager): BasePickerFragment(manager),
    OnItemActionListener<InventoryReport> {
    private var _binding: FragmentPickerInventoryBinding? = null

    private val binding get() = _binding!!
    private val viewModelReport: InventoryReportPickerViewModel by viewModels()
    private val inventoryAdapter = InventoryReportAdapter(this)
    private val inventorySearchAdapter = InventoryReportSearchAdapter(this)

    lateinit var listener: (report: InventoryReport?) -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickerInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.appBar.toolbar, R.string.title_inventory_select, R.menu.menu_picker, { this.dismiss() }) {}

        searchView?.setOnQueryTextFocusChangeListener { _, hasFocus ->
            viewModelReport.onSearchMode = hasFocus

            searchView?.isIconified = !hasFocus
            binding.recyclerView.adapter = if (hasFocus) inventorySearchAdapter else inventoryAdapter
        }
        searchView?.setOnCloseListener {
            dismissSearch()
            viewModelReport.onSearchMode = false
            true
        }

        searchView?.let {
            connectionHandler += viewModelReport.searchBox.connectView(SearchBoxViewAppCompat(it))
        }
        with(binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = inventoryAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        inventorySearchAdapter.addLoadStateListener { _, loadState ->
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

                    if (inventorySearchAdapter.itemCount > 0)
                        binding.recyclerView.show()
                    else binding.emptyView.root.show()
                }
                is LoadState.Error -> binding.errorView.root.hide()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            inventoryAdapter.loadStateFlow.collectLatest {
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
                                inventoryAdapter.itemCount < 1)
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
            viewModelReport.inventories.collectLatest {
                inventoryAdapter.submitData(it)
            }
        }
        viewModelReport.searchResults.observe(viewLifecycleOwner) {
            inventorySearchAdapter.submitList(it)
        }
    }

    override fun onActionPerformed(
        data: InventoryReport?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        if (action == OnItemActionListener.Action.SELECT) {
            listener.invoke(data)
            this.dismiss()
        }
    }

    fun show(listener: (report: InventoryReport?) -> Unit) {
        this.listener = listener
        this.show()
    }

    companion object {
        const val REQUEST_KEY_PICK = "request:pick:inventory"
        const val EXTRA_INVENTORY = "extra:inventory"
    }
}