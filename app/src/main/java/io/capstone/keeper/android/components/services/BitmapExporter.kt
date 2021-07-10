package io.capstone.keeper.android.components.services

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import java.lang.Exception

class BitmapExporter: Service() {

    companion object {
        const val ACTION_EXPORT = "action:export"
        const val ACTION_CANCEL = "action:cancel"
        const val EXTRA_DESTINATION = "extra:destination"
        const val EXTRA_BITMAP = "extra:bitmap"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_EXPORT -> onExport(intent)
            ACTION_CANCEL -> this.stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun onExport(intent: Intent?) {
        val destination = intent?.getParcelableExtra<Uri>(EXTRA_DESTINATION)
        val bitmap = intent?.getParcelableExtra<Bitmap>(EXTRA_BITMAP)

        if (destination != null && bitmap != null) {
            try {
                contentResolver.openOutputStream(destination).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            } catch (ex: Exception) { stopSelf() }
        }
        stopSelf()
    }
}