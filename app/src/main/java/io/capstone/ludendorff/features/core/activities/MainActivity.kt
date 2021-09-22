package io.capstone.ludendorff.features.core.activities

import android.app.NotificationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.widget.Toast
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
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.core.services.NotificationServices
import io.capstone.ludendorff.features.shared.components.BaseActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var controller: NavController

    private val authViewModel: AuthViewModel by viewModels()
    private val assignmentViewModel: AssignmentViewModel by viewModels()

    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var connectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.also {
            when(it.action) {
                ACTION_ASSIGNMENT -> {
                    val id = it.getIntExtra(NotificationServices.EXTRA_NOTIFICATION_ID,
                        0)
                    notificationManager.cancel(id)

                    it.getStringExtra(NotificationServices.EXTRA_PAYLOAD)?.also { payload ->
                        assignmentViewModel.fetch(payload)
                    }
                }
                ACTION_REQUEST -> {
                    TODO()
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
        const val REQUEST_CODE_ASSIGNMENT = 2
        const val EXTRA_CURRENT_DESTINATION = "extra:current:destination"
    }
}