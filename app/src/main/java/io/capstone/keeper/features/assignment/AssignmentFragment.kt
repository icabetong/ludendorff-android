package io.capstone.keeper.features.assignment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.discord.panels.OverlappingPanelsLayout
import io.capstone.keeper.R
import io.capstone.keeper.components.extensions.setup
import io.capstone.keeper.databinding.FragmentAssignmentBinding
import io.capstone.keeper.features.shared.components.BaseFragment

class AssignmentFragment: BaseFragment() {
    private var _binding: FragmentAssignmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssignmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_assignment,
            iconRes = R.drawable.ic_hero_menu,
            onNavigationClicked = { getOverlappingPanelLayout().openStartPanel() },
            menuRes = R.menu.menu_main,
            onMenuOptionClicked = {
                when(it) {
                    R.id.action_menu -> getOverlappingPanelLayout().openEndPanel()
                }
            }
        )

        setupToolbar(binding.appBar.toolbar, {
            getOverlappingPanelLayout().openStartPanel()
        }, R.string.activity_assignment, R.drawable.ic_hero_menu, R.menu.menu_main, { id ->
            when (id) {
                R.id.action_menu -> getOverlappingPanelLayout().openEndPanel()
            }
        })
    }
}