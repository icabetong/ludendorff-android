package io.capstone.ludendorff.features.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.navigation.NavController
import androidx.navigation.Navigation
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.FragmentOptionsUsersBinding
import io.capstone.ludendorff.features.shared.components.BaseFragment

class UserOptionsFragment: BaseFragment() {
    private var _binding: FragmentOptionsUsersBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOptionsUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()

        /**
         *  We will retrieve the NavController here
         *  so that if the activity is recreated
         *  the whole application doesn't crash
         */
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
        binding.departmentButton.setOnClickListener {
            controller?.navigate(R.id.navigation_department)
        }
    }
}