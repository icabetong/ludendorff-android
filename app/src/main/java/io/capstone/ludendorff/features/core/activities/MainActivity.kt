package io.capstone.ludendorff.features.core.activities

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.ActivityMainBinding
import io.capstone.ludendorff.features.assignment.AssignmentViewModel
import io.capstone.ludendorff.features.assignment.editor.AssignmentEditorFragment
import io.capstone.ludendorff.features.auth.AuthViewModel
import io.capstone.ludendorff.features.shared.components.BaseActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var controller: NavController

    private val authViewModel: AuthViewModel by viewModels()
    private val assignmentViewModel: AssignmentViewModel by viewModels()

    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var connectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.also {
            when (it.action) {
                ACTION_ASSIGNMENT -> {
                    it.getStringExtra(EXTRA_PAYLOAD)?.also { assignmentId ->
                        assignmentViewModel.fetch(assignmentId)
                    }
                }
            }
        }

        controller = Navigation.findNavController(this, R.id.navHostFragment)

        if (authViewModel.checkCurrentUser() != null)
            controller.navigate(R.id.to_navigation_root)
        else controller.navigate(R.id.to_navigation_auth)
    }

    override fun onSupportNavigateUp(): Boolean = controller.navigateUp()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        controller.currentDestination?.let {
            outState.putInt(EXTRA_CURRENT_DESTINATION, it.id)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState.getInt(EXTRA_CURRENT_DESTINATION).let {
            controller.navigate(it)
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            assignmentViewModel.assignment.collectLatest {
                if (it != null)
                    controller.navigate(R.id.navigation_editor_assignment,
                        bundleOf(AssignmentEditorFragment.EXTRA_ASSIGNMENT to it))
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    override fun onStop() {
        super.onStop()

        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private var networkCallback = object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            runOnUiThread {

            }
        }
        override fun onLost(network: Network) {
            super.onLost(network)
            runOnUiThread {

            }
        }
    }

    companion object {
        const val ACTION_ASSIGNMENT = "action:assignment"
        const val ACTION_REQUEST = "action:request"
        const val REQUEST_CODE_ASSIGNMENT = 1
        const val EXTRA_PAYLOAD = "extra:payload"
        const val EXTRA_CURRENT_DESTINATION = "extra:current:destination"
    }
}