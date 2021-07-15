package io.capstone.keeper.features.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.components.custom.NavigationItemDecoration
import io.capstone.keeper.components.persistence.UserProperties
import io.capstone.keeper.databinding.FragmentNavigationBinding
import io.capstone.keeper.features.auth.AuthViewModel
import io.capstone.keeper.features.settings.SettingsActivity
import io.capstone.keeper.features.shared.components.BaseFragment
import javax.inject.Inject

@AndroidEntryPoint
class NavigationFragment: BaseFragment(), NavigationAdapter.NavigationItemListener {
    private var _binding: FragmentNavigationBinding? = null
    private var navigationAdapter: NavigationAdapter? = null
    private var controller: NavController? = null

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

        controller = requireActivity().supportFragmentManager.findFragmentById(R.id.navHostFragment)
            ?.findNavController()

        navigationAdapter = NavigationAdapter(activity, R.menu.menu_navigation, R.id.navigation_user_home,
            this@NavigationFragment)
    }

    override fun onStart() {
        super.onStart()

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

                    controller?.navigate(R.id.to_navigation_auth)
                }
                negativeButton(R.string.button_cancel)
            }
        }
    }

    override fun onItemSelected(id: Int) {
        viewModel.setDestination(id)

        navigationAdapter?.setDestination(id)
        dismissNavigationPanel()
    }

    override fun onResume() {
        super.onResume()

        viewModel.destination.observe(viewLifecycleOwner) {
            navigationAdapter?.setDestination(it)
        }
    }

    private fun dismissNavigationPanel() {
        getOverlappingPanelLayout().closePanels()
    }
}