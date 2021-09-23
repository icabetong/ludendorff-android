package io.capstone.ludendorff.features.core.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.custom.CoilProgressDrawable
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.databinding.FragmentRootBinding
import io.capstone.ludendorff.databinding.LayoutDrawerHeaderBinding
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.profile.ProfileFragment
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.user.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RootFragment: BaseFragment() {
    private var _binding: FragmentRootBinding? = null
    private var _headerBinding: LayoutDrawerHeaderBinding? = null
    private var controller: NavController? = null
    private var mainController: NavController? = null
    private var networkSnackbar: Snackbar? = null

    private val binding get() = _binding!!
    private val headerBinding get() = _headerBinding!!
    private val viewModel: CoreViewModel by activityViewModels()

    @Inject lateinit var userProperties: UserProperties

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _headerBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainController = requireActivity().supportFragmentManager.findFragmentById(R.id.navHostFragment)
            ?.findNavController()

        /**
         *  Get this fragment's NavController
         *  to control what fragment will show up
         *  depending on what directions is on the
         *  NavigationViewModel
         */
        controller = (childFragmentManager.findFragmentById(R.id.nestedNavHostFragment)
                as? NavHostFragment)?.navController
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

        viewModel.subscribeToDocumentChanges()
        viewModel.userData.observe(viewLifecycleOwner) {
            setProperties(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.networkStatus.collectLatest {
                if (!it) {
                    networkSnackbar = createSnackbar(
                        R.string.feedback_network_limited_or_unavailable,
                        length = Snackbar.LENGTH_INDEFINITE
                    ).apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            setAction(R.string.button_reconnect) {
                                startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
                            }
                    }
                } else networkSnackbar?.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.navigationView.setNavigationItemSelectedListener {
            try {
                controller?.navigate(it.itemId)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } catch (e: Exception) {
                mainController?.navigate(it.itemId)
            }
            true
        }
        headerBinding.profileImageView.setOnClickListener {
            mainController?.navigate(R.id.navigation_profile, null, null,
                FragmentNavigatorExtras(it to ProfileFragment.TRANSITION_IMAGE)
            )
        }
    }

    private fun setProperties(it: User) {

        headerBinding.nameTextView.text = it.getDisplayName()
        headerBinding.emailTextView.text = it.email
        if (it.imageUrl != null)
            headerBinding.profileImageView.load(it.imageUrl) {
                error(R.drawable.ic_flaticon_user)
                scale(Scale.FILL)
                transformations(CircleCropTransformation())
                placeholder(CoilProgressDrawable(requireContext(), R.color.keeper_primary))
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