package com.example.fluentread.service.screenRecord

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object MediaRecordPermission {
    //Check  READ_MEDIA_VIDEO, WRITE_MEDIA_VIDEO permissions, RECORD_AUDIO
    const val TAG = "ScreenRecordPermission"

    fun isRecordAudioPermissionGranted(context: Context): Boolean {
        val permission = Manifest.permission.RECORD_AUDIO
        val res  = ContextCompat.checkSelfPermission(
            context,
            permission
        )
        return res == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if the READ_MEDIA_VIDEO permission is granted.
     */
    fun isReadMediaVideoPermissionGranted(context: Context): Boolean {
        val permission = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val res = ContextCompat.checkSelfPermission(
            context,
            permission
        )
        return res == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if the WRITE_MEDIA_VIDEO permission is granted.
     */
    fun isWriteMediaVideoPermissionGranted(context: Context): Boolean {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val res = ContextCompat.checkSelfPermission(
            context,
            permission
        )
        return res == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if the POST_NOTIFICATIONS permission is granted.
     */
    fun isNotificationPermissionGranted(context: Context): Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val res = ContextCompat.checkSelfPermission(
                context,
                permission
            )
            return res == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    /**
     * Builds a list of permissions that are not granted.
     * @param context The context to check the permission status.
     * @return An array of permission strings that are not granted.
     */
    fun buildListScreenRecordPermissionNotGranted(context: Context): Array<String> {
        val list = mutableListOf<String>()

        if(!isRecordAudioPermissionGranted(context)) {
            list += Manifest.permission.RECORD_AUDIO
        }

        // Read video
        if(!isReadMediaVideoPermissionGranted(context)) {
            list += if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_VIDEO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }

        // Write (only < Android 10)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            !isWriteMediaVideoPermissionGranted(context)) {
            list += Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

        if(!isNotificationPermissionGranted(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list += Manifest.permission.POST_NOTIFICATIONS
        }

        return list.toTypedArray()
    }


    /**
     * Build a list of audio record permissions that are not granted
     * @param context The context to check the permission status.
     * @return An array of permission strings that are not granted.
     */
    fun buildListAudioRecordPermissionGranted(context: Context): Array<String> {
        val list = mutableListOf<String>()

        if(!isRecordAudioPermissionGranted(context)) {
            list += Manifest.permission.RECORD_AUDIO
        }

        return list.toTypedArray()
    }


}

