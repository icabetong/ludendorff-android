package io.capstone.keeper.android.features.scan

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.view.isVisible
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.ScanMode
import com.discord.panels.OverlappingPanelsLayout
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.components.persistence.DevicePermissions
import io.capstone.keeper.android.databinding.FragmentScanBinding
import io.capstone.keeper.android.features.shared.components.BaseFragment
import javax.inject.Inject

@AndroidEntryPoint
class ScanFragment: BaseFragment() {
    private var _binding: FragmentScanBinding? = null

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var codeScanner: CodeScanner

    private val binding get() = _binding!!

    @Inject lateinit var permissions: DevicePermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                switchViews(it)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        codeScanner = CodeScanner(view.context, binding.codeScannerView)
        with(binding.appBar.toolbar) {
            setTitle(R.string.activity_scan)
            setNavigationIcon(R.drawable.ic_hero_menu)
            setNavigationOnClickListener {
                val activityView: View = requireActivity().findViewById(R.id.overlappingPanels)
                if (activityView is OverlappingPanelsLayout)
                    activityView.openStartPanel()
            }
        }

        with(codeScanner) {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()

        binding.codeScannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()

        if (!permissions.cameraPermissionGranted) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            switchViews(false)
        } else {
            switchViews(true)
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun switchViews(permissionGranted: Boolean) {
        binding.codeScannerView.isVisible = permissionGranted
        binding.errorView.isVisible = !permissionGranted
    }
}