package io.capstone.ludendorff.features.asset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.Navigation
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.FragmentOptionsAssetsBinding
import io.capstone.ludendorff.features.shared.components.BaseFragment

class AssetOptionsFragment: BaseFragment() {
    private var _binding: FragmentOptionsAssetsBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOptionsAssetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        /**
         *  We will retrieve the NavController here
         *  so that if the activity is recreated
         *  the whole application doesn't crash
         */
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
        binding.categoryButton.setOnClickListener {
            controller?.navigate(R.id.to_navigation_category)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.filterCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.filterOptionsLayout.isVisible = isChecked
        }
        binding.sortCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.sortOptionsLayout.isVisible = isChecked
        }
    }
}