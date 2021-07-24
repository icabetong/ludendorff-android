package io.capstone.keeper.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import io.capstone.keeper.R
import io.capstone.keeper.components.extensions.setup
import io.capstone.keeper.databinding.FragmentSettingsBinding
import io.capstone.keeper.features.shared.components.BaseFragment

class SettingsFragment: BaseFragment() {
    private var _binding: FragmentSettingsBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_settings,
            iconRes = R.drawable.ic_hero_arrow_left,
            onNavigationClicked = { controller?.navigateUp() }
        )
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
    }
}