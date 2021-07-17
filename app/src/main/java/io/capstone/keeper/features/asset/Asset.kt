package io.capstone.keeper.features.asset

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Parcelable
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import io.capstone.keeper.components.utils.IDGenerator
import io.capstone.keeper.features.category.Category
import kotlinx.android.parcel.Parcelize
import java.time.ZonedDateTime
import java.util.*

@Parcelize
data class Asset @JvmOverloads constructor(
    var assetId: String = IDGenerator.generateRandom(),
    var assetName: String? = null,
    var dateCreated: ZonedDateTime? = ZonedDateTime.now(),
    var status: Status? = null,
    var category: Category? = null,
    var specifications: Map<String, String> = emptyMap()
): Parcelable {

    fun generateQRCode(): Bitmap {
        return Companion.generateQRCode(assetId)
    }

    fun toAssetCore(): AssetCore {
        return AssetCore.fromAsset(this)
    }

    enum class Status {
        OPERATIONAL,
        IDLE,
        UNDER_MAINTENANCE,
        RETIRED,
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

        fun generateQRCode(id: String): Bitmap {
            val bitMatrix = QRCodeWriter().encode(id, BarcodeFormat.QR_CODE,
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
