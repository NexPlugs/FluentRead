package com.example.fluentread.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.fluentread.service.accessibility.ScrollAccessibilityService
import com.example.fluentread.service.camera.CameraServiceListener
import com.example.fluentread.service.camera.CameraXService
import com.example.fluentread.service.mlk.FaceBehavior
import com.example.fluentread.service.mlk.FaceDetectorService
import com.example.fluentread.service.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AppController: Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val TAG = "AppController"

        const val NOTIFICATION_ID = 0x2001
    }

    //Initialize the service
    private val scrollAccessibilityService: ScrollAccessibilityService? = ScrollAccessibilityService.getInstance()

    private val notificationHelper: NotificationHelper = NotificationHelper(
        this, TAG, "App Controller Service",
    )

    // Coroutine scope for the service
    private var serviceScope: CoroutineScope? = null

    override fun onCreate() {
        super.onCreate()

        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        // Start the camera service
        CameraXService.onCreate(this)

        // Dummy code to start tracking
        startTracking()

        // Start observing camera frames
        observerCameraFrames()

        // Start observing face behavior
        observerFaceBehavior()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startNotificationForeground()

        return START_STICKY
    }

    // Start tracking user eye movement
    fun startTracking() {
        Log.d(TAG, "startTracking: Starting tracking")
        CameraXService.onStartCameraX(this)
    }

    /**
     * Start the service in the foreground with a notification
     * This is important to keep the service running in the background
     */
    @SuppressLint("ForegroundServiceType")
    private fun startNotificationForeground() {

        Log.d(TAG, "Start notification foreground")
        try{
            val notificationBuilder = notificationHelper.initNotificationBuilder()
            notificationHelper.createNotificationChannel()
            startForeground(NOTIFICATION_ID, notificationBuilder.build())
        } catch (e: Exception) {
            Log.d(TAG, "startNotificationForeground: ${e.message}")
        }
    }

    /**
     * Observer camera frames and process them for face detection
     */
    private fun observerCameraFrames() {
        serviceScope?.launch {
            CameraXService.listener.collect { listener ->

                Log.d(TAG, "observerCameraFrames: Camera listener event: $listener")

                when(listener) {
                    is CameraServiceListener.OnImageProxy -> {
                        FaceDetectorService.detectFace(listener.imageProxy)
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
            FaceDetectorService.behavior.collect { behavior ->
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
        CameraXService.onDestroy()
    }


    override fun onDestroy() {
        super.onDestroy()

        // Clean up resources
        CameraXService.onDestroy()
        FaceDetectorService.onDestroy()

        // Clear the coroutine scope
        serviceScope?.cancel()
        serviceScope = null
    }
}