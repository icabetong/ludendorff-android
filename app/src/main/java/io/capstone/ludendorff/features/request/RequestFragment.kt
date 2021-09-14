package io.capstone.ludendorff.features.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.databinding.FragmentRequestBinding
import io.capstone.ludendorff.features.shared.components.BaseFragment

class RequestFragment: BaseFragment() {
    private var _binding: FragmentRequestBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        _binding = FragmentRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.toolbar, arrayOf(binding.swipeRefreshLayout, binding.emptyView.root,
            binding.errorView.root, binding.permissionView.root, binding.shimmerFrameLayout))

        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_requests,
            onNavigationClicked = { controller?.navigateUp() }
        )
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
    }

}