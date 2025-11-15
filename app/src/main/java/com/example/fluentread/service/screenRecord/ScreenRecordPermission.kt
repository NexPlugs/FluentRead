package com.example.fluentread.service.screenRecord

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object ScreenRecordPermission {
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

    fun isWriteMediaVideoPermissionGranted(context: Context): Boolean {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val res = ContextCompat.checkSelfPermission(
            context,
            permission
        )
        return res == PackageManager.PERMISSION_GRANTED
    }


}

