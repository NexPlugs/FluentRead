package com.example.fluentread.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import com.example.fluentread.service.accessibility.ScrollAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * AppPermissions is a utility class that defines constants for various permission request codes used in the application.
 * These constants help in identifying the type of permission being requested when handling permission results.
 */
class AppPermissions {
    companion object {
        const val TAG = "AppPermissions"

        //Singleton instance of AppPermissions
        val INSTANCE: AppPermissions? = null

        fun getInstance(): AppPermissions {
            if (INSTANCE == null) {
                return AppPermissions()
            }
            return INSTANCE
        }

        // Request codes for various permissions
        const val REQUEST_ACCESSIBILITY = 0x1001
        const val REQUEST_OVERLAY = 0x1002
        const val REQUEST_CAMERA = 0x1003

        // Request code for multiple permissions
        const val REQUEST_MULTIPLE = 0x1004
    }

    private var scope: CoroutineScope? = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Reopens the application by launching the main activity.
     * @param context The context to use for launching the activity.
     */
    private fun reOpenApplication(context: Context) {
        try {
            Log.d(TAG, "reOpenApplication: Reopening application")
            val packageManager = context.packageManager
            val packageName = context.packageName
            Log.d(TAG, "reOpenApplication: packageName: $packageName")
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.d(TAG, "reOpenApplication: ${e.message}")
        }
    }


    private fun trackingPermissionStatus(tracking:() ->  Boolean, context: Context) {
        if(scope == null) {
            Log.d(TAG, "trackingPermissionStatus: scope is null")
            return
        }
        scope?.launch {
            while (!tracking.invoke()) {
                Log.d(TAG, "trackingPermissionStatus: Permission not granted yet")
                delay(1000)
            }
            Log.d(TAG, "trackingPermissionStatus: Permission granted")
            reOpenApplication(context)
        }
    }


    /**
     * Requests accessibility permission from the user.
     */
    fun requestAccessibilityPermission(activity: Activity) {

        val context = activity.applicationContext

        if(isAccessibilityPermissionGranted(context)) {
            Log.d(TAG, "requestAccessibilityPermission: Accessibility permission already granted")
            return
        }
        // Start coroutine to monitor permission status
        trackingPermissionStatus( {
            isAccessibilityPermissionGranted(context)
        }, context)


        // Open the accessibility settings first
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            val bundle = Bundle()
            val args = activity.packageName + "/" + ScrollAccessibilityService::class.java.name
            bundle.putString(":settings:fragment_args_key", args)
            putExtras(bundle)
        }
        try {
            activity.startActivityForResult(intent, REQUEST_ACCESSIBILITY)
        } catch (e: Exception) {
            Log.d(TAG, "requestAccessibilityPermission: ${e.message}")
            return
        }
    }

    fun requestOverlayPermission(activity: Activity) {
        val context = activity.applicationContext

        if(isOverlayPermissionGranted(context)) {
            Log.d(TAG, "requestOverlayPermission: Overlay permission already granted")
            return
        }

        // Start coroutine to monitor permission status
        trackingPermissionStatus({
            isOverlayPermissionGranted(context)
        }, context)

        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }

        try {
            activity.startActivityForResult(intent, REQUEST_OVERLAY)
        } catch (e: Exception) {
            Log.d(TAG, "requestOverlayPermission: ${e.message}")
            return
        }

    }

    fun requestReadExternalStoragePermission(activity: Activity) {
        // Implementation for requesting read external storage permission
    }

    /**
     * Requests camera permission from the user.
     * @param activity The activity from which the permission request is initiated.
     */
    fun requestCameraPermission(activity: Activity) {
        val context = activity.applicationContext

        if(isCameraPermissionGranted(context)) {
            Log.d(TAG, "requestCameraPermission: Camera permission already granted")
            return
        }

        // Start coroutine to monitor permission status
        trackingPermissionStatus({
            isCameraPermissionGranted(context)
        }, context)

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }

        try {
            activity.startActivityForResult(intent, REQUEST_CAMERA)
        } catch (e: Exception) {
            Log.d(TAG, "requestCameraPermission: ${e.message}")
            return
        }
    }

    /**
     * Checks if the accessibility permission is granted.
     * @param context The context to access system settings.
     */
    fun isAccessibilityPermissionGranted(context: Context): Boolean {
        val accessibilityEnabled = Settings.Secure.getInt(
            context.contentResolver,
           Settings.Secure.ACCESSIBILITY_ENABLED, 0
        )

        return accessibilityEnabled == 1
    }

    /**
     * Checks if the overlay permission is granted.
     * @param context The context to access system settings.
     */
    fun isOverlayPermissionGranted(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Checks if the read external storage permission is granted.
     * @param context The context to check the permission status.
     */
    fun isReadExternalStoragePermissionGranted(context: Context): Boolean {
        val readPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        val permissionStatus = context.checkSelfPermission(readPermission)
        return permissionStatus == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if the write external storage permission is granted.
     * @param context The context to check the permission status.
     */
    fun isWriteExternalStoragePermissionGranted(context: Context): Boolean {
        val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val permissionStatus = context.checkSelfPermission(writePermission)
        return permissionStatus == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if the camera permission is granted.
     * @param context The context to check the permission status.
     */
    fun isCameraPermissionGranted(context: Context): Boolean {
        val cameraPermission = Manifest.permission.CAMERA
        val permissionStatus = context.checkSelfPermission(cameraPermission)
        return permissionStatus == PackageManager.PERMISSION_GRANTED
    }

    fun areMultiplePermissionsGranted(): Boolean {
        // Implementation to check if multiple permissions are granted
        return false
    }

    // Clear the coroutine scope to prevent memory leaks
    fun clear() {
        scope?.cancel()
        scope = null
    }
}