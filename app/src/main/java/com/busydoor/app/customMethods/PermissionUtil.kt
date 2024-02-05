package com.busydoor.app.customMethods

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {
    const val REQUEST_CODE_CAMERA_PERMISSION = 101

    fun requestCameraAndStoragePermissions(activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permissions if not granted
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_CODE_CAMERA_PERMISSION
            )
            return false
        } else {
            // Permissions already granted
            // You can handle the logic here or just return, depending on your needs
            return true
        }
    }
}