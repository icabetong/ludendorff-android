package io.capstone.keeper.android.features.core.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.FragmentCoreBinding
import io.capstone.keeper.android.features.core.viewmodel.NavigationViewModel
import io.capstone.keeper.android.features.shared.components.BaseFragment

@AndroidEntryPoint
class CoreFragment: BaseFragment() {
    private var _binding: FragmentCoreBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val navigationViewModel: NavigationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller = childFragmentManager.findFragmentById(R.id.navigationHostFragment)
            ?.findNavController()

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
            controller?.navigate(it)
        }
    }
}