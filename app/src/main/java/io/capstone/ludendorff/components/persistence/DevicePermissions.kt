package io.capstone.ludendorff.components.persistence

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

    val readStoragePermissionGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED
            else true
        }
}