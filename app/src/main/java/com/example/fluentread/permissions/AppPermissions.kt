package com.example.fluentread.permissions

import android.app.Activity

/**
 * AppPermissions is a utility class that defines constants for various permission request codes used in the application.
 * These constants help in identifying the type of permission being requested when handling permission results.
 */
class AppPermissions {
    companion object {
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

    /**
     * Requests accessibility permission from the user.
     */
    fun requestAccessibilityPermission(activity: Activity) {

    }

    fun requestOverlayPermission(activity: Activity) {
        // Implementation for requesting overlay permission
    }


    fun requestCameraPermission(activity: Activity) {
        // Implementation for requesting camera permission
    }

    fun requestMultiplePermissions(activity: Activity) {
        // Implementation for requesting multiple permissions
    }

    fun isAccessibilityPermissionGranted(): Boolean {
        // Implementation to check if accessibility permission is granted
        return false
    }

    fun isOverlayPermissionGranted(): Boolean {
        // Implementation to check if overlay permission is granted
        return false
    }

    fun isCameraPermissionGranted(): Boolean {
        // Implementation to check if camera permission is granted
        return false
    }

    fun areMultiplePermissionsGranted(): Boolean {
        // Implementation to check if multiple permissions are granted
        return false
    }

}