package io.capstone.ludendorff.features.issued

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.databinding.FragmentIssuedBinding
import io.capstone.ludendorff.features.shared.BaseFragment

class IssuedReportFragment: BaseFragment() {
    private var _binding: FragmentIssuedBinding? = null
    private var controller: NavController? = null
    private var mainController: NavController? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val drawer = getNavigationDrawer()
                    if (drawer?.isDrawerOpen(GravityCompat.START) == true)
                        drawer.closeDrawer(GravityCompat.START)
                    else controller?.navigateUp()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIssuedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
        mainController = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(binding.root, binding.appBar.toolbar, emptyArray(), binding.actionButton)

        binding.actionButton.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_issued,
            iconRes = R.drawable.ic_round_menu_24,
            onNavigationClicked = { triggerNavigationDrawer() },
        )
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            mainController?.navigate(R.id.navigation_editor_issued, null, null,
                FragmentNavigatorExtras(it to TRANSITION_NAME_ROOT)
            )
        }
    }
}