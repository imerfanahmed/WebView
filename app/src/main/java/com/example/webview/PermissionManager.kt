package com.example.webview

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: Activity) {
    companion object {
        const val STORAGE_PERMISSION_REQUEST_CODE = 1001
        
        private val STORAGE_PERMISSIONS_BELOW_33 = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        private val STORAGE_PERMISSIONS_33_AND_ABOVE = arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    }

    fun checkAndRequestStoragePermissions(): Boolean {
        return if (hasStoragePermissions()) {
            true
        } else {
            requestStoragePermissions()
            false
        }
    }

    private fun hasStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            STORAGE_PERMISSIONS_33_AND_ABOVE.all { permission ->
                ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            STORAGE_PERMISSIONS_BELOW_33.all { permission ->
                ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    private fun requestStoragePermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            STORAGE_PERMISSIONS_33_AND_ABOVE
        } else {
            STORAGE_PERMISSIONS_BELOW_33
        }
        
        ActivityCompat.requestPermissions(
            activity,
            permissions,
            STORAGE_PERMISSION_REQUEST_CODE
        )
    }

    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }
        }
    }
}