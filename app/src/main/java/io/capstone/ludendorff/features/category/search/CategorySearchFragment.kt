package io.capstone.ludendorff.features.category.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.algolia.instantsearch.helper.android.list.autoScrollToStart
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.helper.android.searchbox.connectView
import com.google.android.material.transition.MaterialSharedAxis
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentSearchBinding
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.shared.BaseSearchFragment

class CategorySearchFragment: BaseSearchFragment(), OnItemActionListener<Category> {
    private var _binding: FragmentSearchBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val searchAdapter = CategorySearchAdapter(this)
    private val viewModel: CategorySearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    controller?.navigateUp()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.toolbar)

        with(binding.recyclerView) {
            itemAnimator = null
            adapter = searchAdapter
            autoScrollToStart(searchAdapter)
        }

        connection += viewModel.searchBox.connectView(SearchBoxViewAppCompat(binding.searchTextView))
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()

        searchAdapter.addLoadStateListener { _, loadState ->
            if (binding.swipeRefreshLayout.isRefreshing)
                binding.swipeRefreshLayout.isRefreshing = false

            when(loadState) {
                LoadState.Loading -> {
                    binding.recyclerView.hide()
                    binding.shimmerFrameLayout.show()
                }
                is LoadState.NotLoading -> {
                    binding.recyclerView.show()
                    binding.shimmerFrameLayout.hide()
                }
                is LoadState.Error -> createSnackbar(R.string.error_generic)
            }
        }
        viewModel.assets.observe(viewLifecycleOwner) {
            searchAdapter.submitList(it)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.swipeRefreshLayout.setOnRefreshListener {

        }
    }

    override fun onActionPerformed(
        data: Category?,
        action: OnItemActionListener.Action,
        container: View?
    ) {

    }
}