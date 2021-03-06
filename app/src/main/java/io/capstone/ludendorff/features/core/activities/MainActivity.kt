package io.capstone.ludendorff.features.core.activities

import android.app.NotificationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.databinding.ActivityMainBinding
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.shared.BaseActivity
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var controller: NavController

    private val viewModel: CoreViewModel by viewModels()

    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var connectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller = Navigation.findNavController(this, R.id.navHostFragment)

        if (viewModel.checkCurrentUser() != null)
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
            viewModel.setNetworkStatus(true)
        }
        override fun onLost(network: Network) {
            super.onLost(network)
            viewModel.setNetworkStatus(false)
        }
    }

    companion object {
        const val ACTION_ASSIGNMENT = "action:assignment"
        const val ACTION_REQUEST = "action:request"
        const val REQUEST_CODE_ASSIGNMENT = 2
        const val EXTRA_CURRENT_DESTINATION = "extra:current:destination"
    }
}