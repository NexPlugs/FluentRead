package com.example.fluentread.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.fluentread.service.camera.CameraService
import com.example.fluentread.service.camera.CameraServiceListener
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
    private val cameraService: CameraService = CameraService.getInstance()
    private val faceDetectorService: FaceDetectorService? = FaceDetectorService.getInstance()

    // Coroutine scope for the service
    private var serviceScope: CoroutineScope? = null

    override fun onCreate() {
        super.onCreate()

        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        // Start the camera service
        cameraService.onCreate()

        // Dummy code to start tracking
        startTracking()

        // Start observing camera frames
        observerCameraFrames()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun startTracking() {
        cameraService.openCamera()
    }

    /**
     * Observer camera frames and process them for face detection
     */
    private fun observerCameraFrames() {
        serviceScope?.launch {
            cameraService.listener.collect { listener ->

                Log.d(TAG, "observerCameraFrames: Camera listener event: $listener")

                when(listener) {
                    is CameraServiceListener.OnImageAvailable -> {
                        faceDetectorService?.detectFace(listener.image)
                    }
                    else -> {}
                }
            }
        }
    }

    fun stopTracking() {
        cameraService.closeCamera()
    }


    override fun onDestroy() {
        super.onDestroy()

        // Clean up resources
        cameraService.onDestroy()

        serviceScope?.cancel()
        serviceScope = null
    }
}