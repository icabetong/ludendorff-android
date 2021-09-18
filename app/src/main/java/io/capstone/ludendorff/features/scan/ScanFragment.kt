package io.capstone.ludendorff.features.scan

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.persistence.DevicePermissions
import io.capstone.ludendorff.databinding.FragmentScanBinding
import io.capstone.ludendorff.features.scan.image.ImagePickerBottomSheet
import io.capstone.ludendorff.features.shared.components.BaseFragment
import javax.inject.Inject

@AndroidEntryPoint
class ScanFragment: BaseFragment(), FragmentResultListener {
    private var _binding: FragmentScanBinding? = null
    private var controller: NavController? = null

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var readStorageLauncher: ActivityResultLauncher<String>
    private lateinit var codeScanner: CodeScanner

    private val binding get() = _binding!!
    private val viewModel: ScanViewModel by activityViewModels()

    @Inject lateinit var permissions: DevicePermissions

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

        readStorageLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission())  { isGranted ->
                if (isGranted)
                    ImagePickerBottomSheet(childFragmentManager)
                        .show()
            }

        cameraPermissionLauncher =
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
        setInsets(
            binding.root, binding.appBar.toolbar, arrayOf(binding.errorView, binding.codeScannerView),
            binding.actionButton
        )

        binding.appBar.appBar.setExpanded(false)
        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_scan,
            iconRes = R.drawable.ic_hero_menu,
            onNavigationClicked = { triggerNavigationDrawer() }
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
                ScanResultFragment(childFragmentManager)
                    .show()
            }
        }

        switchViews(permissions.cameraPermissionGranted)
        registerForFragmentResult(arrayOf(ImagePickerBottomSheet.REQUEST_KEY_PICK),
            this)
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
    }

    override fun onResume() {
        super.onResume()

        binding.actionButton.setOnClickListener {
            if (permissions.readStoragePermissionGranted)
                ImagePickerBottomSheet(childFragmentManager)
                    .show()
            else readStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        binding.codeScannerView.setOnClickListener {
            codeScanner.startPreview()
        }
        binding.permissionButton.setOnClickListener {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun switchViews(permissionGranted: Boolean) {
        binding.codeScannerView.isVisible = permissionGranted
        binding.errorView.isVisible = !permissionGranted
        if (permissionGranted)
            codeScanner.startPreview()
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            ImagePickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Uri>(ImagePickerBottomSheet.EXTRA_IMAGE_URI)?.let { uri ->
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

                            viewModel.setDecodedResult(decodeResult.text)
                            ScanResultFragment(childFragmentManager)
                                .show()
                        } catch (exception: NotFoundException) {
                            createSnackbar(R.string.error_decode_not_found)

                        } catch (exception: ChecksumException) {
                            createSnackbar(R.string.error_decode_checksum)

                        } catch (exception: FormatException) {
                            createSnackbar(R.string.error_decode_format)

                        } catch (exception: Exception) {
                            createSnackbar(R.string.error_generic)
                        }
                    }
                }
            }
        }
    }
}