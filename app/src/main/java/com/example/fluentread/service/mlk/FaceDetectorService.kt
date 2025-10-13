package com.example.fluentread.service.mlk

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * A foreground service that handles face detection operations using ML Kit.
 * This service is designed to run in the foreground to ensure it remains active
 * while performing face detection tasks.
 */
object FaceDetectorService {

    const val TAG = "FaceDetectorService"

    const val CENTER_THRESHOLD = 10


    /**
     * ML Kit Face Detector instance configured with options suitable for real-time detection.
     * The detector is set to fast performance mode, with all landmarks and classifications enabled.
     * This configuration is ideal for applications that require quick detection and analysis of facial features.
     */
    private var faceDetector: FaceDetector? = null

    // A flag to check if the service is initialized
    private var isInitialized = false

    private val detectScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var _faceBehavior: MutableSharedFlow<FaceBehavior> = MutableSharedFlow()
    val behavior: SharedFlow<FaceBehavior> get() = _faceBehavior

    private fun emitBehavior(behavior: FaceBehavior) {
        detectScope.launch { _faceBehavior.emit(behavior) }
    }


    init {

        // High-accuracy landmark detection and face classification
        faceDetector = FaceDetection.getClient(
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

    /**
     * Detect faces in the given image and emit face behavior based on orientation.
     * @param inputImage The image to process for face detection.
     * @throws Exception if face detection fails.
     */
    @OptIn(ExperimentalGetImage::class)
    fun detectFace(inputImage: ImageProxy) {
        if(!isInitialized || faceDetector == null) {
            Log.d(TAG, "detectFace: FaceDetectorService is not initialized")
            return
        }

        val mediaImage = inputImage.image
        if (mediaImage == null) {
            Log.d(TAG, "detectFace: MediaImage is null")
            return
        }

        Log.d(TAG, "Image info: ${inputImage.imageInfo}")
        // Convert the input image to an ML Kit InputImage
        try {
            val image = InputImage.fromMediaImage(mediaImage, inputImage.imageInfo.rotationDegrees)

            // Process the image using the face detector
            faceDetector?.process(image)
                // Add a success listener to handle the detected faces
                ?.addOnSuccessListener { faces ->
                    // Task completed successfully
                    // ...
                    if(faces.isEmpty()) {
                        Log.d(TAG, "detectFace: No faces detected")
                        return@addOnSuccessListener
                    }
                    val faceDetail = faces[0]
                    val angelX = faceDetail.headEulerAngleX  // Head is rotated to the right rotX degrees
                    val angelY = faceDetail.headEulerAngleY  // Head is rotated to the right rotY degrees

                    //  Determine face orientation based on Euler angles
                    when {
                        angelX < -CENTER_THRESHOLD -> {
                            Log.d(TAG, "detectFace: Face is looking up: $angelX")
                            emitBehavior(FaceBehavior.UP)
                        }
                        angelX > CENTER_THRESHOLD -> {
                            Log.d(TAG, "detectFace: Face is looking down: $angelX")
                            emitBehavior(FaceBehavior.DOWN)
                        }
                        angelY < -CENTER_THRESHOLD -> {
                            Log.d(TAG, "detectFace: Face is looking left: $angelY")
                            emitBehavior(FaceBehavior.CENTER)
                        }
                    }
                    inputImage.close()
                }
        } catch (e: Exception) {
            Log.d(TAG, "detectFace: Error detecting face: ${e.message}")
        }
    }

    /**
     * CLean up resources when the service is destroyed
     */
    fun onDestroy() {
        Log.d(TAG, "onDestroy: Destroying FaceDetectorService")

        try {
            faceDetector?.close()

            detectScope.cancel()
        } catch (e: Exception) {
            Log.d(TAG, "onDestroy: Error destroying FaceDetectorService: ${e.message}")
        }
    }
}