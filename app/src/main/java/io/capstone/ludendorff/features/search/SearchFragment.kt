package io.capstone.ludendorff.features.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import io.capstone.ludendorff.databinding.FragmentSearchBinding
import io.capstone.ludendorff.features.shared.components.BaseFragment

class SearchFragment: BaseFragment() {
    private var _binding: FragmentSearchBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.toolbar)

        binding.root.transitionName = TRANSITION_NAME
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
    }

    override fun onResume() {
        super.onResume()
        binding.searchTextInputLayout.setStartIconOnClickListener {
            controller?.navigateUp()
        }
        binding.searchTextInputLayout.setEndIconOnClickListener {  }
    }

    companion object {
        const val TRANSITION_NAME = "transition:search"
    }
}