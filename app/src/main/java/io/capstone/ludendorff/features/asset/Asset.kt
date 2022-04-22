package io.capstone.ludendorff.features.asset

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.category.CategoryCore
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Asset @JvmOverloads constructor(
    var stockNumber: String = "",
    var description: String? = null,
    var subcategory: String? = null,
    var category: CategoryCore? = null,
    var unitOfMeasure: String? = null,
    var unitValue: Double = 0.0,
    var remarks: String? = null,
): Parcelable {

    companion object {
        const val COLLECTION = "assets"
        const val FIELD_STOCK_NUMBER = "stockNumber"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_CATEGORY = "category"
        const val FIELD_CATEGORY_ID = "${FIELD_CATEGORY}.${Category.FIELD_ID}"
        const val FIELD_CATEGORY_NAME = "${FIELD_CATEGORY}.${Category.FIELD_NAME}"
        const val FIELD_SUBCATEGORY = "subcategory"
        const val FIELD_UNIT_OF_MEASURE = "unitOfMeasure"
        const val FIELD_UNIT_VALUE = "unitValue"
        const val FIELD_REMARKS = "remarks"

        const val ID_PREFIX = "clsu://ludendorff/"

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Asset>() {
            override fun areContentsTheSame(oldAsset: Asset, newAsset: Asset): Boolean {
                return oldAsset == newAsset
            }

            override fun areItemsTheSame(oldAsset: Asset, newAsset: Asset): Boolean {
                return oldAsset.stockNumber == newAsset.stockNumber
            }
        }

        fun generateQRCode(number: String): Bitmap {
            val bitMatrix = QRCodeWriter().encode(
                "$ID_PREFIX${number}", BarcodeFormat.QR_CODE,
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
