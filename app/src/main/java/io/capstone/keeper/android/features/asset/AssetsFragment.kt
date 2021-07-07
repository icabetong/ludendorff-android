package io.capstone.keeper.android.features.asset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.discord.panels.OverlappingPanelsLayout
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.FragmentAssetsBinding
import io.capstone.keeper.android.features.shared.components.BaseFragment

class AssetsFragment: BaseFragment() {
    private var _binding: FragmentAssetsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssetsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.appBar.toolbar) {
            setTitle(R.string.activity_assets)
            setNavigationIcon(R.drawable.ic_hero_menu)
            setNavigationOnClickListener {
                val overlappingPanels = getViewFromActivity(R.id.overlappingPanels)
                if (overlappingPanels is OverlappingPanelsLayout)
                    overlappingPanels.openStartPanel()
            }
        }
    }
}