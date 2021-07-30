package io.capstone.keeper.features.scan

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.components.extensions.setup
import io.capstone.keeper.components.persistence.DevicePermissions
import io.capstone.keeper.databinding.FragmentScanBinding
import io.capstone.keeper.features.shared.components.BaseFragment
import javax.inject.Inject

@AndroidEntryPoint
class ScanFragment: BaseFragment(), BaseFragment.CascadeMenuDelegate {
    private var _binding: FragmentScanBinding? = null

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var codeScanner: CodeScanner

    private val binding get() = _binding!!
    private val viewModel: ScanViewModel by activityViewModels()

    @Inject lateinit var permissions: DevicePermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                switchViews(it)
            }

        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                requireContext().contentResolver.openInputStream(uri)?.use {
                    val bitmap = BitmapFactory.decodeStream(it)
                    val reader = MultiFormatReader()
                    try {
                        val width = bitmap.width
                        val height = bitmap.height
                        val pixels = IntArray(width * height)
                        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

                        val source = RGBLuminanceSource(width, height, pixels)
                        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                        val decodeResult = reader.decode(binaryBitmap)
                        android.util.Log.e("DEBUG", decodeResult.text)
                    } catch (exception: NotFoundException) {
                        android.util.Log.e("DEBUG", exception.toString())
                    } catch (exception: ChecksumException) {
                        android.util.Log.e("DEBUG", exception.toString())
                    } catch (exception: FormatException) {
                        android.util.Log.e("DEBUG", exception.toString())
                    }
                }
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

        binding.appBar.appBar.setExpanded(false)
        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_scan,
            iconRes = R.drawable.ic_hero_menu,
            onNavigationClicked = { getOverlappingPanelLayout().openStartPanel() },
            menuRes = R.menu.menu_main,
            onMenuOptionClicked = ::onMenuItemClicked
        )

        codeScanner = CodeScanner(view.context, binding.codeScannerView)

        with(codeScanner) {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false
        }

        codeScanner.decodeCallback = DecodeCallback {
            viewModel.setDecodedResult(it.text)
            activity?.runOnUiThread {
                getOverlappingPanelLayout().openEndPanel()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        binding.actionButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        binding.codeScannerView.setOnClickListener {
            codeScanner.startPreview()
        }
        binding.permissionButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onResume() {
        super.onResume()

        if (permissions.cameraPermissionGranted) {
            switchViews(true)
            codeScanner.startPreview()
        } else switchViews(false)
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun switchViews(permissionGranted: Boolean) {
        binding.codeScannerView.isVisible = permissionGranted
        binding.errorView.isVisible = !permissionGranted
    }

    override fun onMenuItemClicked(@IdRes id: Int) {
        when(id) {
            R.id.action_menu -> {
                getOverlappingPanelLayout().openEndPanel()
            }
        }
    }
}