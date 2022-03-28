package io.capstone.ludendorff.features.issued.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.algolia.instantsearch.helper.android.list.autoScrollToStart
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.helper.android.searchbox.connectView
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentSearchIssueBinding
import io.capstone.ludendorff.features.issued.editor.IssuedReportEditorFragment
import io.capstone.ludendorff.features.shared.BaseSearchFragment

class IssuedReportSearchFragment: BaseSearchFragment(), OnItemActionListener<IssuedReportSearch> {
    private var _binding: FragmentSearchIssueBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val searchAdapter = IssuedReportSearchAdapter(this)
    private val viewModel: IssuedReportSearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
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
        _binding = FragmentSearchIssueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.toolbar)

        binding.appBar.transitionName = TRANSITION_SEARCH
        binding.toolbar.setup(onNavigationClicked = { controller?.navigateUp()})

        with(binding.recyclerView) {
            itemAnimator = null
            adapter = searchAdapter
            autoScrollToStart(searchAdapter)
        }

        connection += viewModel.searchBox.connectView(SearchBoxViewAppCompat(binding.searchTextView))
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()

        searchAdapter.addLoadStateListener { _, loadState ->
            when(loadState) {
                LoadState.Loading -> {
                    binding.recyclerView.hide()
                    binding.emptyView.root.hide()
                    binding.shimmerFrameLayout.show()
                }
                is LoadState.NotLoading -> {
                    binding.recyclerView.hide()
                    binding.emptyView.root.hide()
                    binding.shimmerFrameLayout.hide()

                    if (searchAdapter.itemCount > 0)
                        binding.recyclerView.show()
                    else binding.emptyView.root.show()
                }
                is LoadState.Error -> createSnackbar(R.string.error_generic)
            }
        }
        viewModel.issuedReport.observe(viewLifecycleOwner) {
            searchAdapter.submitList(it)
        }
    }

    override fun onStop() {
        super.onStop()
        hideKeyboardFromCurrentFocus(binding.root)
    }

    override fun onActionPerformed(
        data: IssuedReportSearch?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        if (action == OnItemActionListener.Action.SELECT) {
            container?.let {
                controller?.navigate(R.id.navigation_editor_issued,
                    bundleOf(IssuedReportEditorFragment.EXTRA_ISSUED_REPORT
                            to data?.toIssuedReport()))
            }
        }
    }
}