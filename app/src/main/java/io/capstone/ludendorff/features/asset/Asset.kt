package io.capstone.ludendorff.features.asset

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.Timestamp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.category.CategoryCore
import kotlinx.parcelize.Parcelize

@Parcelize
data class Asset @JvmOverloads constructor(
    var assetId: String = IDGenerator.generateRandom(),
    var assetName: String? = null,
    var dateCreated: Timestamp? = Timestamp.now(),
    var status: Status? = null,
    var category: CategoryCore? = null,
    var specifications: Map<String, String> = emptyMap(),
): Parcelable {

    fun generateQRCode(): Bitmap {
        return Companion.generateQRCode(assetId)
    }

    fun minimize(): AssetCore {
        return AssetCore.from(this)
    }

    enum class Status {
        OPERATIONAL,
        IDLE,
        UNDER_MAINTENANCE,
        RETIRED;

        @StringRes
        fun getStringRes(): Int {
            return when(this) {
                OPERATIONAL -> R.string.asset_status_option_operational
                IDLE -> R.string.asset_status_option_idle
                UNDER_MAINTENANCE -> R.string.asset_status_option_under_maintenance
                RETIRED -> R.string.asset_status_option_retired
            }
        }
    }

    companion object {
        const val COLLECTION = "assets"
        const val FIELD_ID = "assetId"
        const val FIELD_NAME = "assetName"
        const val FIELD_DATE_CREATED = "dateCreated"
        const val FIELD_STATUS = "status"
        const val FIELD_CATEGORY = "category"
        const val FIELD_CATEGORY_ID = "${FIELD_CATEGORY}.${Category.FIELD_ID}"
        const val FIELD_SPECIFICATIONS = "specifications"

        const val ID_PREFIX = "clsu://keeper/"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Asset>() {
            override fun areContentsTheSame(oldItem: Asset, newItem: Asset): Boolean {
                return oldItem.assetId == newItem.assetId
            }

            override fun areItemsTheSame(oldItem: Asset, newItem: Asset): Boolean {
                return oldItem == newItem
            }
        }

        fun generateQRCode(id: String): Bitmap {
            val bitMatrix = QRCodeWriter().encode(
                "${ID_PREFIX}${id}", BarcodeFormat.QR_CODE,
                128, 128)
            val bitmap = Bitmap.createBitmap(bitMatrix.width, bitMatrix.height,
                Bitmap.Config.RGB_565)
            val pixels = IntArray(bitMatrix.width * bitMatrix.height)

            for (y in 0 until bitMatrix.height) {
                val offset = y * bitMatrix.width
                for (x in 0 until bitMatrix.width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            bitmap.setPixels(pixels, 0, bitMatrix.width, 0, 0, bitMatrix.width, bitMatrix.height)

            return bitmap
        }
    }
}