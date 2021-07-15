package io.capstone.keeper.features.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import io.capstone.keeper.R
import io.capstone.keeper.databinding.FragmentOptionsUsersBinding
import io.capstone.keeper.features.shared.components.BaseFragment

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
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
    }

    override fun onStart() {
        super.onStart()

        binding.departmentButton.setOnClickListener {
            controller?.navigate(R.id.navigation_department)
        }
    }
}