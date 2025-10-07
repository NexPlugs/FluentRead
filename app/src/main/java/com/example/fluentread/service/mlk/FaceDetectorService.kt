package com.example.fluentread.service.mlk

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * A foreground service that handles face detection operations using ML Kit.
 * This service is designed to run in the foreground to ensure it remains active
 * while performing face detection tasks.
 */
class FaceDetectorService: Service() {

    companion object {
        const val TAG = "FaceDetectorService"
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}