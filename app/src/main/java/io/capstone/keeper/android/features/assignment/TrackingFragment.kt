package io.capstone.keeper.android.features.assignment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.discord.panels.OverlappingPanelsLayout
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.FragmentTrackingBinding
import io.capstone.keeper.android.features.shared.components.BaseFragment

class TrackingFragment: BaseFragment() {
    private var _binding: FragmentTrackingBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(binding.appBar.toolbar, {
            val rootView: View? = getParentView()?.findViewById(R.id.overlappingPanels)

            if (rootView is OverlappingPanelsLayout)
                rootView.openStartPanel()
        }, R.string.activity_tracking, R.drawable.ic_hero_menu)
    }
}