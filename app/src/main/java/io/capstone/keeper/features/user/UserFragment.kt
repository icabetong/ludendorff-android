package io.capstone.keeper.features.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.discord.panels.OverlappingPanelsLayout
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.databinding.FragmentUsersBinding
import io.capstone.keeper.features.shared.components.BaseFragment

@AndroidEntryPoint
class UserFragment: BaseFragment() {
    private var _binding: FragmentUsersBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.actionButton.transitionName = TRANSITION_NAME_ROOT

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        setupToolbar(binding.appBar.toolbar, {
            getOverlappingPanelLayout().openStartPanel()
        }, R.string.activity_users, R.drawable.ic_hero_menu, R.menu.menu_main, { id ->
            when (id) {
                R.id.action_menu -> getOverlappingPanelLayout().openEndPanel()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        binding.actionButton.setOnClickListener {
            controller?.navigate(R.id.to_navigation_editor_user, null, null,
                FragmentNavigatorExtras(it to TRANSITION_NAME_ROOT))
        }
    }

}