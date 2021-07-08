package io.capstone.keeper.android.features.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.discord.panels.OverlappingPanelsLayout
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.components.custom.NavigationItemDecoration
import io.capstone.keeper.android.components.persistence.UserProperties
import io.capstone.keeper.android.databinding.FragmentNavigationBinding
import io.capstone.keeper.android.features.auth.AuthActivity
import io.capstone.keeper.android.features.auth.AuthViewModel
import io.capstone.keeper.android.features.core.viewmodel.NavigationViewModel
import io.capstone.keeper.android.features.settings.SettingsActivity
import io.capstone.keeper.android.features.shared.components.BaseFragment
import javax.inject.Inject

@AndroidEntryPoint
class NavigationFragment: BaseFragment(), NavigationAdapter.NavigationItemListener {
    private var _binding: FragmentNavigationBinding? = null
    private var navigationAdapter: NavigationAdapter? = null

    private val binding get() = _binding!!
    private val viewModel: NavigationViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var userProperties: UserProperties

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNavigationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationAdapter = NavigationAdapter(activity, R.menu.menu_navigation, R.id.navigation_user_home,
            this@NavigationFragment)

        with(binding.recyclerView) {
            addItemDecoration(NavigationItemDecoration(context))
            adapter = navigationAdapter
        }

        binding.nameTextView.text = userProperties.getDisplayName()

        binding.navigationSettings.setOnClickListener {
            startActivity(Intent(context, SettingsActivity::class.java))
        }
        binding.navigationEndSession.setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(R.string.dialog_sign_out_title)
                message(R.string.dialog_sign_out_message)
                positiveButton(R.string.button_continue) {
                    authViewModel.endSession()
                    userProperties.clear()

                    startActivity(Intent(context, AuthActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                negativeButton(R.string.button_cancel)
            }
        }
    }

    override fun onItemSelected(id: Int) {
        viewModel.setDestination(id)
        navigationAdapter?.setNewDestination(id)
        dismissNavigationPanel()
    }

    private fun dismissNavigationPanel() {
        val view: View = requireActivity().findViewById(R.id.overlappingPanels)
        if (view is OverlappingPanelsLayout)
            view.closePanels()
    }
}