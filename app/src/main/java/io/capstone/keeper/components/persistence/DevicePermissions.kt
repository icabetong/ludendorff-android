package io.capstone.keeper.components.persistence

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class DevicePermissions(private val context: Context) {

    val cameraPermissionGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                context.checkSelfPermission(Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
            else true
        }
}