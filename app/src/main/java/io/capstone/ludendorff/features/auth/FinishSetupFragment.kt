package io.capstone.ludendorff.features.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.FragmentFinishSetupBinding
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.shared.BaseFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FinishSetupFragment: BaseFragment() {
    private var _binding: FragmentFinishSetupBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val coreViewModel: CoreViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinishSetupBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = requireActivity().supportFragmentManager.findFragmentById(R.id.navHostFragment)
            ?.findNavController()

        binding.finishButton.setOnClickListener {
            val currentPassword = binding.currentPasswordTextInput.text.toString()
            val password = binding.passwordTextInput.text.toString()
            val confirmPassword = binding.confirmPasswordTextInput.text.toString()

            if (password != confirmPassword) {
                createSnackbar(R.string.error_invalid_credentials)
                return@setOnClickListener
            }

            coreViewModel.changePassword(currentPassword, password)
        }
    }

    override fun onStart() {
        super.onStart()

        viewLifecycleOwner.lifecycleScope.launch {
            coreViewModel.finishSetup.collect {
                when(it) {
                    is Response.Error ->
                        createSnackbar(R.string.error_invalid_credentials)
                    is Response.Success -> {
                        createSnackbar(R.string.feedback_account_setup_finished)
                        controller?.navigate(FinishSetupFragmentDirections.toNavigationRoot())
                    }
                }
            }
        }
    }
}