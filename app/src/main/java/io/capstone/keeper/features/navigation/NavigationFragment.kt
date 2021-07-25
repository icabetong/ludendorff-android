package io.capstone.keeper.features.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.components.custom.NavigationItemDecoration
import io.capstone.keeper.databinding.FragmentNavigationBinding
import io.capstone.keeper.features.shared.components.BaseFragment

@AndroidEntryPoint
class NavigationFragment: BaseFragment(), NavigationAdapter.NavigationItemListener {
    private var _binding: FragmentNavigationBinding? = null
    private var navigationAdapter: NavigationAdapter? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: NavigationViewModel by activityViewModels()

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
        with(binding.recyclerView) {
            addItemDecoration(NavigationItemDecoration(context))
            adapter = navigationAdapter
        }

        binding.nameTextView.text = viewModel.fullName
        binding.profileImageView.load(viewModel.imageUrl) {
            error(R.drawable.ic_hero_user)
            placeholder(CircularProgressDrawable(requireContext()))
            transformations(CircleCropTransformation())
            scale(Scale.FILL)
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.destination.observe(viewLifecycleOwner) {
            navigationAdapter?.setDestination(it)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.profileImageView.setOnClickListener {
            controller?.navigate(R.id.navigation_profile)
        }
        binding.navigationSettings.setOnClickListener {
            controller?.navigate(R.id.navigation_settings)
        }
        binding.navigationEndSession.setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(R.string.dialog_sign_out_title)
                message(R.string.dialog_sign_out_message)
                positiveButton(R.string.button_continue) {
                    viewModel.endSession()

                    controller?.navigate(R.id.to_navigation_auth)
                }
                negativeButton(R.string.button_cancel)
            }
        }
    }

    private fun dismissNavigationPanel() {
        getOverlappingPanelLayout().closePanels()
    }

    override fun onItemSelected(id: Int) {
        viewModel.setDestination(id)

        navigationAdapter?.setDestination(id)
        dismissNavigationPanel()
    }


}