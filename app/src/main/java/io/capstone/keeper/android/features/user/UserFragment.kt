package io.capstone.keeper.android.features.user

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
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.FragmentUsersBinding
import io.capstone.keeper.android.features.shared.components.BaseFragment

@AndroidEntryPoint
class UserFragment: BaseFragment() {
    private var _binding: FragmentUsersBinding? = null
    private var controller: NavController? = null

    private lateinit var adapter: UserAdapter

    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels()

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
            val rootView: View? = getParentView()?.findViewById(R.id.overlappingPanels)

            if (rootView is OverlappingPanelsLayout)
                rootView.openStartPanel()
        }, R.string.activity_users, R.drawable.ic_hero_menu)

        adapter = UserAdapter()
        binding.recyclerView.adapter = adapter
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