package com.example.fluentread.service.camera

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraXService to manage camera operations using CameraX library
 * - Singleton pattern to ensure only one instance of the service
 */
class CameraXService : Service(), LifecycleOwner{

    companion object {
        const val TAG = "CameraXService"

        private var INSTANCE: CameraXService? = null

        fun getInstance(): CameraXService? = INSTANCE
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // CameraX attributes

    // Lifecycle registry to manage the lifecycle of the service
    private lateinit var lifeCycleRegistry: LifecycleRegistry

    // Implementing LifecycleOwner to manage the lifecycle of the service
    override val lifecycle: Lifecycle
        get() = lifeCycleRegistry


    //[cameraProvider] is the camera provider instance
    private var cameraProvider: ProcessCameraProvider? = null
    //[cameraProviderFuture] is the future instance of the camera provider
    private var analysisUseCase: ImageAnalysis? = null
    //[cameraExecutor] is the executor service for camera operations
    private var cameraExecutor: ExecutorService? = null

    private var isCameraInitialized = false

    private var preview: Preview? = null

    // Flow to emit camera service events
    private var _listener: MutableSharedFlow<CameraServiceListener> = MutableSharedFlow()
    val listener: SharedFlow<CameraServiceListener> get() = _listener

    private fun emitEvent(event: CameraServiceListener) {
        _listener.tryEmit(event)
    }


    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        try {

            lifeCycleRegistry = LifecycleRegistry(this)
            lifeCycleRegistry.currentState = Lifecycle.State.CREATED

            // Initialize CameraX
            cameraExecutor = Executors.newSingleThreadExecutor()

            onStartCameraX()
            isCameraInitialized = true

            emitEvent(CameraServiceListener.OnCameraOpened)

        } catch (e: Exception) {
            Log.d(TAG, "onCreate: Error initializing CameraX: ${e.message}")
            isCameraInitialized = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(!isCameraInitialized) stopSelf()
        return START_STICKY
    }

    /**
     * Starts the CameraX service and initializes camera provider
     */
    fun onStartCameraX() {
        Log.d(TAG, "onStartCameraX: Starting CameraX")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindUseCase()
            } catch (e: Exception) {
                Log.d(TAG, "onStartCameraX: Error starting CameraX: ${e.message}")
            }
            // Bind use cases here
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Binds the camera use cases to the lifecycle
     * - Sets up ImageAnalysis use case with backpressure strategy
     * - Emits image frames through the listener flow
     * - Handles exceptions during binding
     * @throws Exception if binding fails
     */
    fun bindUseCase() {
        val provider = cameraProvider ?: return
        val executor = cameraExecutor ?: return

        // Unbind use cases before rebinding
        provider.unbindAll()

        // Select back camera as a default
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        val analyzer = ImageAnalysis.Builder()
            // Only keep the latest frame
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                // Set the analyzer to process frames
                setAnalyzer(executor, FaceAnalyzer())
            }

        // Bind use cases to camera
        try {
            provider.bindToLifecycle(
                this,
                cameraSelector,
                analyzer
            )
        } catch (e: Exception) {
            Log.d(TAG, "bindUseCase: Error binding use cases: ${e.message}")
        }
    }

    /**
     * Opens the camera flash if available
     * - Binds to the back camera and enables torch mode
     * - Handles exceptions during flash operation
     */
    fun openFlash() {
        val provider = cameraProvider ?: return

        try {
            provider.unbindAll()

            val camera = provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA
            )
            camera.cameraControl.enableTorch(true)

        } catch (e: Exception) {
            Log.d(TAG, "openFlash: Error opening flash: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Destroying CameraXService")
        try {
            // Clean up resources
            cameraExecutor?.shutdown()
            cameraProvider?.unbindAll()
            lifeCycleRegistry.currentState = Lifecycle.State.DESTROYED

            // Clear the singleton instance
            INSTANCE = null
        } catch (e: Exception) {
            Log.d(TAG, "onDestroy: Error destroying CameraXService: ${e.message}")
        }

        emitEvent(CameraServiceListener.OnCameraDisconnected)
    }


    /**
     * Analyzer class to process camera frames for face detection
     */
    private inner class FaceAnalyzer : ImageAnalysis.Analyzer{

        @ExperimentalGetImage
        override fun analyze(proxy: ImageProxy) {
            val mediaImage = proxy.image
            if(mediaImage != null) {
                emitEvent(CameraServiceListener.OnImageProxy(proxy))
                emitEvent(CameraServiceListener.OnImageAvailable(mediaImage))

                proxy.close()
            } else {
                proxy.close()
            }
        }

    }
}
