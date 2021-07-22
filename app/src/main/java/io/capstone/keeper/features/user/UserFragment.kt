package io.capstone.keeper.features.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.components.custom.GenericItemDecoration
import io.capstone.keeper.components.extensions.setup
import io.capstone.keeper.components.interfaces.OnItemActionListener
import io.capstone.keeper.databinding.FragmentUsersBinding
import io.capstone.keeper.features.shared.components.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserFragment: BaseFragment(), OnItemActionListener<User> {
    private var _binding: FragmentUsersBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: UserViewModel by activityViewModels()
    private val userAdapter = UserAdapter(this)

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

        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_users,
            iconRes = R.drawable.ic_hero_menu,
            onNavigationClicked = { getOverlappingPanelLayout().openStartPanel() },
            menuRes = R.menu.menu_main,
            onMenuOptionClicked = {
                when(it) {
                    R.id.action_menu -> getOverlappingPanelLayout().openEndPanel()
                }
            }
        )

        with(binding.recyclerView) {
            addItemDecoration(GenericItemDecoration(context))
            adapter = userAdapter
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.users.collectLatest {
                userAdapter.submitData(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            controller?.navigate(R.id.to_navigation_editor_user, null, null,
                FragmentNavigatorExtras(it to TRANSITION_NAME_ROOT))
        }
    }

    override fun onActionPerformed(
        data: User?,
        action: OnItemActionListener.Action,
        container: View?
    ) {

    }

}