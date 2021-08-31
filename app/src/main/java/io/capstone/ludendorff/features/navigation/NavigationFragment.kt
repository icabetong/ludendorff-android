package io.capstone.ludendorff.features.navigation


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.FragmentNavigationBinding
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.profile.ProfileFragment
import io.capstone.ludendorff.features.shared.components.BaseFragment
import io.capstone.ludendorff.features.user.User

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
        binding.profileImageView.transitionName = ProfileFragment.TRANSITION_IMAGE

        controller = requireActivity().supportFragmentManager.findFragmentById(R.id.navHostFragment)
            ?.findNavController()
    }

    override fun onStart() {
        super.onStart()

        val progressDrawable = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 4f
            centerRadius = 24f
            setTint(ContextCompat.getColor(requireContext(), R.color.keeper_primary))
            start()
        }

        coreViewModel.userData.observe(viewLifecycleOwner) {
            binding.nameTextView.text = it.getDisplayName()
            if (it.imageUrl != null)
                binding.profileImageView.load(it.imageUrl) {
                    error(R.drawable.ic_hero_user)
                    placeholder(progressDrawable)
                    transformations(CircleCropTransformation())
                    scale(Scale.FILL)
                }
            else binding.profileImageView.setImageResource(R.drawable.ic_flaticon_user)

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
            controller?.navigate(R.id.navigation_profile, null, null,
                FragmentNavigatorExtras(it to ProfileFragment.TRANSITION_IMAGE))
        }
        binding.navigationView.setNavigationItemSelectedListener {
            viewModel.setDestination(it.itemId)
            dismissNavigationPanel()

            true
        }
        binding.navigationNotification.setOnClickListener {
            controller?.navigate(R.id.navigation_notification)
        }
        binding.navigationCoreSettings.setOnClickListener {
            controller?.navigate(R.id.navigation_core_settings)
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