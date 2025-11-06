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
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import com.example.fluentread.R
import com.example.fluentread.service.accessibility.ScrollAccessibilityService
import com.example.fluentread.service.camera.CameraServiceListener
import com.example.fluentread.service.camera.CameraXService
import com.example.fluentread.service.mlk.FaceBehavior
import com.example.fluentread.service.mlk.FaceDetectorService
import com.example.fluentread.service.notification.NotificationHelper
import com.example.fluentread.service.overlay.ToggleView
import com.example.fluentread.service.overlay.compose.Toggle
import com.example.fluentread.ui.theme.FluentReadTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that coordinates:
 * - CameraX for frame capture
 * - ML Kit for face detection
 * - Accessibility for auto-scrolling based on user’s gaze
 * - ToggleView overlay for user control
 */
@AndroidEntryPoint
class AppController : Service() {
    @Inject lateinit var appControllerRepository: AppControllerRepository

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "AppController"
        private const val NOTIFICATION_ID = 0x2001
    }


    // State flow for shared app data

    val cameraIsRunning: Boolean get() = appControllerRepository.appData.value.cameraIsRunning

    // Initialization flag
    private var _isInitialized = false
    val isInitialized: Boolean get() = _isInitialized

    // Convenience property to check if eye tracking is active
    val isEyeTracking: Boolean get() {
        if(!isInitialized) return false
        return cameraIsRunning
    }

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
        CameraXService.onCreate()
        _isInitialized = true
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
        appControllerRepository.updateAppData( cameraIsRunning = true)
    }

    private fun stopTracking() {
        Log.d(TAG, "stopTracking: Stopping face tracking.")
        CameraXService.onStopCameraX()
        appControllerRepository.updateAppData( cameraIsRunning = false)
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
            rootGroup?.addView(ComposeView(context).apply {
                setContent {
                    FluentReadTheme { Toggle(
                        onUp = { scrollAccessibilityService?.scrollUp() },
                        onDown = { scrollAccessibilityService?.scrollDown() },
                    ) }
                }
            })
        }
    }

    // Create the toggle button view
    private fun createToggleButton(): ImageView = ImageView(this).apply {
        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.app_icon)).apply {
            layoutParams = ViewGroup.LayoutParams(100, 100)
        }
        setOnClickListener {
            if (cameraIsRunning) stopTracking() else startTracking()
        }
    }
    // endregion

    // region === Notification ===
    @SuppressLint("ForegroundServiceType")
    private fun startNotificationForeground() {
        runCatching {
            val builder = notificationHelper.initNotificationBuilder()
            notificationHelper.createNotificationChannel()
            startForeground(NOTIFICATION_ID, builder.build())
        }.onFailure {
            Log.e(TAG, "Failed to start foreground notification: ${it.message}", it)
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
