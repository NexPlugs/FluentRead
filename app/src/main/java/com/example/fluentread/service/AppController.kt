package com.example.fluentread.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.Point
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.fluentread.R
import com.example.fluentread.service.accessibility.ScrollAccessibilityService
import com.example.fluentread.service.camera.CameraServiceListener
import com.example.fluentread.service.camera.CameraXService
import com.example.fluentread.service.mlk.FaceBehavior
import com.example.fluentread.service.mlk.FaceDetectorService
import com.example.fluentread.service.notification.NotificationHelper
import com.example.fluentread.service.overlay.ToggleView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Foreground service that coordinates:
 * - CameraX for frame capture
 * - ML Kit for face detection
 * - Accessibility for auto-scrolling based on user’s gaze
 * - ToggleView overlay for user control
 */
class AppController : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "AppController"
        private const val NOTIFICATION_ID = 0x2001
    }

    // State flow for shared app data
    private val _data = MutableStateFlow(AppData())
    val data: StateFlow<AppData> get() = _data

    // Dependencies
    private val notificationHelper by lazy {
        NotificationHelper(this, TAG, "App Controller Service")
    }

    private val scrollAccessibilityService: ScrollAccessibilityService? by lazy {
        ScrollAccessibilityService.getInstance()
    }

    // Coroutine scope for service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // UI Overlay
    private var toggleView: ToggleView? = null

    // region === Lifecycle ===
    override fun onCreate() {
        super.onCreate()
        ensureOverlayPermission()
        CameraXService.onCreate(applicationContext)
        Log.d(TAG, "Service created.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotificationForeground()
        if (Settings.canDrawOverlays(this)) {
            if (toggleView == null) createToggle()
            toggleView?.show()
        } else {
            Log.w(TAG, "Overlay permission not granted; stopping service.")
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed.")
        cleanupResources()
        super.onDestroy()
    }
    // endregion

    // region === Tracking Control ===
    private fun startTracking() {
        Log.d(TAG, "startTracking: Starting face tracking.")
        CameraXService.onStartCameraX(this)
        observeCameraFrames()
        observeFaceBehavior()
        _data.value = _data.value.copy(cameraIsRunning = true)
    }

    private fun stopTracking() {
        Log.d(TAG, "stopTracking: Stopping face tracking.")
        CameraXService.onStopCameraX()
        _data.value = _data.value.copy(cameraIsRunning = false)
    }
    // endregion

    // region === Observers ===
    private fun observeCameraFrames() {
        serviceScope.launch {
            CameraXService.events.collect { event ->
                when (event) {
                    is CameraServiceListener.OnImageProxy -> FaceDetectorService.detectFace(event.imageProxy)
                    else -> Unit
                }
            }
        }
    }

    private fun observeFaceBehavior() {
        serviceScope.launch {
            FaceDetectorService.behavior.collect { behavior ->
                when (behavior) {
                    FaceBehavior.UP -> scrollAccessibilityService?.scrollUp()
                    FaceBehavior.DOWN -> scrollAccessibilityService?.scrollDown()
                    FaceBehavior.CENTER -> Unit
                    else -> {
                        Log.w(TAG, "observeFaceBehavior: Unknown behavior: $behavior")
                    }
                }
            }
        }
    }
    // endregion

    // region === Overlay ===
    private fun createToggle() {
        Log.d(TAG, "createToggle: Creating toggle bubble.")
        toggleView = ToggleView(context = this, startPoint = Point(0, 200)).apply {
            rootGroup?.addView(createToggleButton())
        }
    }

    // Create the toggle button view
    private fun createToggleButton(): ImageView = ImageView(this).apply {
        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.app_icon))
        layoutParams = ViewGroup.LayoutParams(100, 100)
        setOnClickListener {
            if (_data.value.cameraIsRunning) stopTracking() else startTracking()
        }
    }
    // endregion

    // region === Notification ===
    @SuppressLint("ForegroundServiceType")
    private fun startNotificationForeground() {
        try {
            val builder = notificationHelper.initNotificationBuilder()
            notificationHelper.createNotificationChannel()
            startForeground(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground notification: ${e.message}", e)
        }
    }
    // endregion

    // region === Helpers ===
    private fun ensureOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            throw SecurityException("Overlay permission not granted.")
        }
    }

    private fun cleanupResources() {
        serviceScope.cancel()
        toggleView?.remove()
        toggleView = null
        CameraXService.onDestroy()
        FaceDetectorService.onDestroy()
    }
    // endregion
}
