package io.capstone.keeper.android.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.discord.panels.OverlappingPanelsLayout
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.FragmentHomeBinding
import io.capstone.keeper.android.features.shared.components.BaseFragment

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

        with(binding.appBar.toolbar) {
            title = getString(R.string.activity_home)
            setNavigationOnClickListener {
                val activityView: View = requireActivity().findViewById(R.id.overlappingPanels)
                if (activityView is OverlappingPanelsLayout)
                    activityView.openStartPanel()
            }
        }
    }
}