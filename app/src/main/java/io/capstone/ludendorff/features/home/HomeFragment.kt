package io.capstone.ludendorff.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.databinding.FragmentHomeBinding
import io.capstone.ludendorff.features.shared.components.BaseFragment

class HomeFragment: BaseFragment() {
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setInsets(binding.root, binding.appBar.toolbar, binding.actionButton)
        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_home,
            iconRes = R.drawable.ic_hero_menu,
            onNavigationClicked = { triggerNavigationDrawer() },
            menuRes = R.menu.menu_main,
            onMenuOptionClicked = { triggerNavigationDrawer() }
        )
    }

}