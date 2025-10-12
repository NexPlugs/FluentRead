package com.example.fluentread.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.fluentread.service.accessibility.ScrollAccessibilityService
import com.example.fluentread.service.camera.CameraServiceListener
import com.example.fluentread.service.camera.CameraXService
import com.example.fluentread.service.mlk.FaceBehavior
import com.example.fluentread.service.mlk.FaceDetectorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AppController: Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val TAG = "AppController"
    }

    //Initialize the service
    private val cameraService: CameraXService? = CameraXService.getInstance()
    private val faceDetectorService: FaceDetectorService? = FaceDetectorService.getInstance()
    private val scrollAccessibilityService: ScrollAccessibilityService? = ScrollAccessibilityService.getInstance()

    // Coroutine scope for the service
    private var serviceScope: CoroutineScope? = null

    override fun onCreate() {
        super.onCreate()

        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        // Start the camera service
        cameraService?.onCreate()

        // Dummy code to start tracking
        startTracking()

        // Start observing camera frames
        observerCameraFrames()

        // Start observing face behavior
        observerFaceBehavior()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    // Start tracking user eye movement
    fun startTracking() {
        Log.d(TAG, "startTracking: Starting tracking")
        cameraService?.onStartCameraX()
    }

    /**
     * Observer camera frames and process them for face detection
     */
    private fun observerCameraFrames() {
        serviceScope?.launch {
            cameraService?.listener?.collect { listener ->

                Log.d(TAG, "observerCameraFrames: Camera listener event: $listener")

                when(listener) {
                    is CameraServiceListener.OnImageProxy -> {
                        faceDetectorService?.detectFace(listener.imageProxy)
                    }
                    else -> {}
                }
            }
        }
    }


    /**
     * Observer face behavior and perform actions based on detected behavior
     */
    private fun observerFaceBehavior() {
        serviceScope?.launch {
            faceDetectorService?.behavior?.collect { behavior ->

                Log.d(TAG, "observerFaceBehavior: Face behavior event: $behavior")

                when(behavior) {
                    FaceBehavior.UP -> {
                        scrollAccessibilityService?.scrollUp()
                    }
                    FaceBehavior.DOWN -> {
                        scrollAccessibilityService?.scrollDown()
                    }
                    FaceBehavior.CENTER -> {
                        // Do nothing
                    }
                    else -> {}
                }
            }
        }
    }

    fun stopTracking() {
        Log.d(TAG, "stopTracking: Stopping tracking")
        cameraService?.onDestroy()
    }


    override fun onDestroy() {
        super.onDestroy()

        // Clean up resources
        cameraService?.onDestroy()
        faceDetectorService?.onDestroy()

        // Clear the coroutine scope
        serviceScope?.cancel()
        serviceScope = null
    }
}