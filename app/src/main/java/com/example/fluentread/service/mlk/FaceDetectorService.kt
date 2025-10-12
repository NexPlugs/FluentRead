package com.example.fluentread.service.mlk

import android.media.Image
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions

/**
 * A foreground service that handles face detection operations using ML Kit.
 * This service is designed to run in the foreground to ensure it remains active
 * while performing face detection tasks.
 */
class FaceDetectorService {

    companion object {
        const val TAG = "FaceDetectorService"

        //Singleton instance of FaceDetectorService
        var INSTANCE: FaceDetectorService? = null

        fun getInstance(): FaceDetectorService? {
            return INSTANCE
        }

    }

    /**
     * ML Kit Face Detector instance configured with options suitable for real-time detection.
     * The detector is set to fast performance mode, with all landmarks and classifications enabled.
     * This configuration is ideal for applications that require quick detection and analysis of facial features.
     */
    private var faceDetector: FaceDetector? = null

    // A flag to check if the service is initialized
    private var isInitialized = false

    init {

        INSTANCE = this
        // High-accuracy landmark detection and face classification
        INSTANCE!!.faceDetector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                // Use fast performance mode for real-time applications
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                // Enable all landmarks (e.g., eyes, nose, mouth)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                // Enable all classification (e.g., smiling, eyes open)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()
        )

        isInitialized = true
    }

    fun detectFace(inputImage: Image) {
        if(!isInitialized || faceDetector == null) {
            Log.d(TAG, "detectFace: FaceDetectorService is not initialized")
            return
        }

        // Convert the input image to an ML Kit InputImage
        val image = InputImage.fromMediaImage(inputImage, 0)

        // Process the image using the face detector
        faceDetector?.process(image)
            // Add a success listener to handle the detected faces
            ?.addOnSuccessListener { faces ->
                // Task completed successfully
                // ...
                Log.d(TAG, "detectFace: Faces detected: ${faces.size}")
            }
    }
}