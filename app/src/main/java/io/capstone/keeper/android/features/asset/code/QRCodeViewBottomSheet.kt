package io.capstone.keeper.android.features.asset.code

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import coil.load
import com.google.zxing.WriterException
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.FragmentViewQrcodeBinding
import io.capstone.keeper.android.features.asset.Asset
import io.capstone.keeper.android.features.shared.components.BaseBottomSheet

class QRCodeViewBottomSheet(manager: FragmentManager): BaseBottomSheet(manager) {
    private var _binding: FragmentViewQrcodeBinding? = null

    private val binding get() = _binding!!

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
    }

    companion object {
        const val EXTRA_ASSET_ID = "extra:asset:id"
    }
}