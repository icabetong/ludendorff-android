package io.capstone.keeper.features.core.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.databinding.FragmentRootBinding
import io.capstone.keeper.features.navigation.NavigationViewModel
import io.capstone.keeper.features.shared.components.BaseFragment

@AndroidEntryPoint
class RootFragment: BaseFragment() {
    private var _binding: FragmentRootBinding? = null
    private var mainNavController: NavController? = null
    private var endPanelNavController: NavController? = null

    private val binding get() = _binding!!
    private val navigationViewModel: NavigationViewModel by activityViewModels()

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         *  Get this fragment's NavController
         *  to control what fragment will show up
         *  depending on what directions is on the
         *  NavigationViewModel
         */
        val nestedNavHost = childFragmentManager.findFragmentById(R.id.nestedNavHostFragment) as? NavHostFragment
        mainNavController = nestedNavHost?.navController
        /**
         *  Get this other fragment's NavController
         */
        val endPanelNavHost = childFragmentManager.findFragmentById(R.id.endPanelNavHostFragment) as? NavHostFragment
        endPanelNavController = endPanelNavHost?.navController

        /**
         *  Essential to enable the transitions between the
         *  inner fragments
         */
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()

        navigationViewModel.destination.observe(viewLifecycleOwner) {
            mainNavController?.navigate(it)
            if (it == R.id.navigation_scan)
                endPanelNavController?.navigate(R.id.navigation_scan_result)
            else endPanelNavController?.navigate(R.id.navigation_options)
        }
    }
}