package io.capstone.keeper.features.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import io.capstone.keeper.databinding.FragmentNavigationBinding
import io.capstone.keeper.features.core.viewmodel.CoreViewModel
import io.capstone.keeper.features.shared.components.BaseFragment
import io.capstone.keeper.features.user.User

@AndroidEntryPoint
class NavigationFragment: BaseFragment() {
    private var _binding: FragmentNavigationBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val viewModel: NavigationViewModel by activityViewModels()
    private val coreViewModel: CoreViewModel by activityViewModels()

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
    }

    override fun onStart() {
        super.onStart()

        coreViewModel.userData.observe(viewLifecycleOwner) {

            binding.nameTextView.text = it.getDisplayName()
            binding.profileImageView.load(it.imageUrl) {
                error(R.drawable.ic_hero_user)
                placeholder(CircularProgressDrawable(requireContext()))
                transformations(CircleCropTransformation())
                scale(Scale.FILL)
            }

            with(binding.navigationView.menu) {
                findItem(R.id.navigation_users)
                    .isVisible = it.hasPermission(User.PERMISSION_MANAGE_USERS) ||
                        it.hasPermission(User.PERMISSION_ADMINISTRATIVE)

                findItem(R.id.navigation_assignments)
                    .isVisible = it.hasPermission(User.PERMISSION_ADMINISTRATIVE)

                findItem(R.id.navigation_assets)
                    .isVisible = it.hasPermission(User.PERMISSION_READ)
                        || it.hasPermission(User.PERMISSION_ADMINISTRATIVE)

                findItem(R.id.navigation_scan)
                    .isVisible = it.hasPermission(User.PERMISSION_READ)
                        || it.hasPermission(User.PERMISSION_ADMINISTRATIVE)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.profileImageView.setOnClickListener {
            controller?.navigate(R.id.navigation_profile)
        }
        binding.navigationView.setNavigationItemSelectedListener {
            viewModel.setDestination(it.itemId)
            dismissNavigationPanel()

            true
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

}