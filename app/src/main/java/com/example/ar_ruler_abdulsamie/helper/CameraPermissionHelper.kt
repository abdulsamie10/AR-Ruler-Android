package com.example.ar_ruler_abdulsamie.helper

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat

/** Helper to ask camera permission.  */
object CameraPermissionHelper {
    // Camera permission string
    private const val CAMERA_PERMISSION = Manifest.permission.CAMERA

    /** Check to see if the app has the necessary camera permission.
     *
     * @param activity The current activity.
     * @return True if the camera permission is granted, false otherwise.
     */
    fun hasCameraPermission(activity: Activity?): Boolean {
        return (ContextCompat.checkSelfPermission(activity!!, CAMERA_PERMISSION)
                == PackageManager.PERMISSION_GRANTED)
    }

    /** Launch the application settings to grant camera permission manually.
     *
     * @param activity The current activity.
     */
    fun launchPermissionSettings(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }
}
