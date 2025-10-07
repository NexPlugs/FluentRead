package com.example.fluentread.service.camera

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * A foreground service that handles camera operations.
 * This service is designed to run in the foreground to ensure it remains active
 * while performing camera-related tasks.
 */
class CameraService: Service() {

    companion object {
        const val TAG = "CameraService"
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}

