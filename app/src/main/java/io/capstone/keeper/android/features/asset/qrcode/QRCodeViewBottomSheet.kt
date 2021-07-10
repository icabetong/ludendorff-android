package io.capstone.keeper.android.features.asset.qrcode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentManager
import coil.load
import com.google.zxing.WriterException
import io.capstone.keeper.android.R
import io.capstone.keeper.android.components.extensions.startForegroundServiceCompat
import io.capstone.keeper.android.components.services.BitmapExporter
import io.capstone.keeper.android.databinding.FragmentViewQrcodeBinding
import io.capstone.keeper.android.features.asset.Asset
import io.capstone.keeper.android.features.shared.components.BaseBottomSheet

class QRCodeViewBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentViewQrcodeBinding? = null

    private lateinit var saveLauncher: ActivityResultLauncher<Intent>

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        saveLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val exporterService = Intent(context, BitmapExporter::class.java).apply {
                    action = BitmapExporter.ACTION_EXPORT
                    data = it.data?.data
                }
                context?.startForegroundServiceCompat(exporterService)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewQrcodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            it.getString(EXTRA_ASSET_ID)?.also { id ->
                try {
                    val code = Asset.generateQRCode(id)
                    binding.qrCodeImageView.load(code)
                } catch (writerException: WriterException) {
                    createToast(R.string.error_generic)
                } catch (exception: Exception) {
                    createToast(R.string.error_generic)
                }
            }
        }

        binding.saveButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/png"
                putExtra(Intent.EXTRA_TITLE, EXTRA_DEFAULT_NAME)
            }

            saveLauncher.launch(intent)
        }
    }

    companion object {
        const val EXTRA_ASSET_ID = "extra:asset:id"
        const val EXTRA_DEFAULT_NAME = "qrcode.png"
    }
}