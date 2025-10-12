package com.example.fluentread.service.mlk

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
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


    /**
     * Special use for CameraX ImageAnalysis to analyze the image frames.
     */
    private inner class ImageAnalyzer: ImageAnalysis.Analyzer {
        @OptIn(ExperimentalGetImage::class)
        override fun analyze(image: ImageProxy) {
            val mediaImage = image.image
        }
    }
}