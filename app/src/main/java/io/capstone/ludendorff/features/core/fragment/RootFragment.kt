package io.capstone.ludendorff.features.core.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.FragmentRootBinding
import io.capstone.ludendorff.databinding.LayoutDrawerHeaderBinding
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.profile.ProfileFragment
import io.capstone.ludendorff.features.shared.components.BaseFragment
import io.capstone.ludendorff.features.user.User

@AndroidEntryPoint
class RootFragment: BaseFragment() {
    private var _binding: FragmentRootBinding? = null
    private var _headerBinding: LayoutDrawerHeaderBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val headerBinding get() = _headerBinding!!
    private val viewModel: CoreViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRootBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _headerBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller = requireActivity().supportFragmentManager.findFragmentById(R.id.navHostFragment)
            ?.findNavController()

        /**
         *  Get this fragment's NavController
         *  to control what fragment will show up
         *  depending on what directions is on the
         *  NavigationViewModel
         */
        val nestedNavHost = childFragmentManager.findFragmentById(R.id.nestedNavHostFragment)
                as? NavHostFragment
        nestedNavHost?.navController?.let {
            binding.navigationView.setupWithNavController(it)
        }
        if (binding.navigationView.headerCount > 0) {
            _headerBinding = LayoutDrawerHeaderBinding
                .bind(binding.navigationView.getHeaderView(0))
            headerBinding.profileImageView.transitionName = ProfileFragment.TRANSITION_IMAGE
        }

        /**
         *  Essential to enable the transitions between the
         *  inner fragments
         */
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()

        val progressDrawable = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 4f
            centerRadius = 24f
            setTint(ContextCompat.getColor(requireContext(), R.color.keeper_primary))
            start()
        }

        viewModel.userData.observe(viewLifecycleOwner) {
            headerBinding.nameTextView.text = it.getDisplayName()
            if (it.imageUrl != null)
                headerBinding.profileImageView.load(it.imageUrl) {
                    error(R.drawable.ic_flaticon_user)
                    placeholder(progressDrawable)
                    transformations(CircleCropTransformation())
                    scale(Scale.FILL)
                }
            else headerBinding.profileImageView.setImageResource(R.drawable.ic_flaticon_user)

            with(binding.navigationView.menu) {
                findItem(R.id.navigation_assignments)
                    .isVisible = it.hasPermission(User.PERMISSION_ADMINISTRATIVE)

                findItem(R.id.navigation_users)
                    .isVisible = it.hasPermission(User.PERMISSION_MANAGE_USERS) ||
                        it.hasPermission(User.PERMISSION_ADMINISTRATIVE)

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

        headerBinding.profileImageView.setOnClickListener {
            controller?.navigate(R.id.navigation_profile, null, null,
                FragmentNavigatorExtras(it to ProfileFragment.TRANSITION_IMAGE)
            )
        }
    }
}