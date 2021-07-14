package io.capstone.keeper.features.asset

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Parcelable
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.parcel.Parcelize
import java.time.ZonedDateTime
import java.util.*

@Parcelize
data class Asset @JvmOverloads constructor(
    var assetId: String = UUID.randomUUID().toString(),
    var assetName: String? = null,
    var dateCreated: ZonedDateTime? = ZonedDateTime.now(),
    var status: Status? = null,
    var category: String? = null,
    var specifications: Map<String, String> = emptyMap()
): Parcelable {

    fun generateQRCode(): Bitmap {
        return Companion.generateQRCode(assetId)
    }

    enum class Status {
        OPERATIONAL,
        IDLE,
        UNDER_MAINTENANCE,
        RETIRED,
    }

    companion object {

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
