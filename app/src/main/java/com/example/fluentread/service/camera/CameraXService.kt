package com.example.fluentread.service.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object CameraXService : LifecycleOwner {

    private const val TAG = "CameraXService"

    // Lifecycle registry to manage the lifecycle of the service
    private val lifeCycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }
    override val lifecycle: Lifecycle get() = lifeCycleRegistry

    // CameraX variables
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor: ExecutorService? = null
    private var isCameraInitialized = false
    private var isCameraRunning = false

    // Coroutine scope for the service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // SharedFlow to emit camera events
    private val _listener = MutableSharedFlow<CameraServiceListener>(replay = 0)
    val listener: SharedFlow<CameraServiceListener> get() = _listener

    private fun emitEvent(event: CameraServiceListener) {
        serviceScope.launch { _listener.emit(event) }
    }

    /**
     * Initialize the CameraX service
     * @param context The context to use for initializing the camera
     */
    fun onCreate(context: Context) {
        Log.d(TAG, "onCreate: Initializing CameraXService")
        try {
            lifeCycleRegistry.currentState = Lifecycle.State.CREATED
            cameraExecutor = Executors.newSingleThreadExecutor()
            onStartCameraX(context)
            isCameraInitialized = true
        } catch (e: Exception) {
            Log.d(TAG, "onCreate: Error initializing CameraX: ${e.message}")
            isCameraInitialized = false
        }
    }

    /**
     * Start the CameraX service
     * @param context The context to use for starting the camera
     */
    fun onStartCameraX(context: Context) {
        if (isCameraRunning) {
            Log.d(TAG, "onStartCameraX: Camera is already running")
            return
        }

        // Ensure the camera is initialized
        lifeCycleRegistry.currentState = Lifecycle.State.RESUMED
        Log.d(TAG, "onStartCameraX: Starting CameraX")
        try {
            // Get the camera provider
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            // Add a listener to the camera provider future
            cameraProviderFuture.addListener({
                try {
                    cameraProvider = cameraProviderFuture.get()
                    bindUseCase()
                } catch (e: Exception) {
                    Log.d(TAG, "onStartCameraX: Error starting CameraX: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            Log.d(TAG, "onStartCameraX: Error starting CameraX: ${e.message}")
            isCameraRunning = false
        }
    }


    /**
     * Bind the camera use case to the lifecycle
     * - Uses the front camera by default
     * - Sets up an image analyzer to process camera frames
     */
    fun bindUseCase() {
        val provider = cameraProvider ?: return
        val executor = cameraExecutor ?: return
        // Unbind any previous use cases
        provider.unbindAll()

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        // Set up the image analyzer
        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(executor, FaceAnalyzer()) }
        try {
            if (!provider.hasCamera(cameraSelector)) {
                Log.d(TAG, "bindUseCase: No camera available")
                emitEvent(CameraServiceListener.OnCameraError("No camera available"))
                return
            }
            // Bind the camera to the lifecycle
            provider.bindToLifecycle(this, cameraSelector, analyzer)
            isCameraRunning = true
            emitEvent(CameraServiceListener.OnCameraOpened)
        } catch (e: Exception) {
            Log.d(TAG, "bindUseCase: Error binding use cases: ${e.message}")
            onStopCameraX()
        }
    }

    /**
     * Stop the camera and release resources
     */
    fun onStopCameraX() {
        if (!isCameraRunning) {
            Log.d(TAG, "onStopCameraX: Camera is not running")
            return
        }
        Log.d(TAG, "onStopCameraX: Stopping CameraX")
        try {
            cameraProvider?.unbindAll()
            lifeCycleRegistry.currentState = Lifecycle.State.CREATED
            isCameraRunning = false
            emitEvent(CameraServiceListener.OnCameraDisconnected)
        } catch (e: Exception) {
            Log.d(TAG, "onStopCameraX: Error stopping CameraX: ${e.message}")
            isCameraRunning = true
        }
    }

    /**
     * Turn on the camera flash (torch mode)
     * - Only works if the device has a flash
     * - Uses the back camera
     */
    fun openFlash() {
        val provider = cameraProvider ?: return
        try {
            provider.unbindAll()
            val camera = provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA)
            camera.cameraControl.enableTorch(true)
        } catch (e: Exception) {
            Log.d(TAG, "openFlash: Error opening flash: ${e.message}")
        }
    }

    /**
     * Image analyzer to process camera frames
     * - Emits OnImageProxy and OnImageAvailable events
     * - Closes the ImageProxy after processing
     */
    private class FaceAnalyzer : ImageAnalysis.Analyzer {
        val LIMIT_BUFFER_MS = 100L

        private var lastEmitTime = 0L



        init {
            //Count down every second
            CoroutineScope(Dispatchers.Default).launch {
                while (true) {
                    delay(LIMIT_BUFFER_MS)
                    lastEmitTime = 0L
                }
            }
        }

        @ExperimentalGetImage
        override fun analyze(proxy: ImageProxy) {
            Log.d(TAG, "analyze: Analyzing image: ${proxy.imageInfo}")
            try {
                val image = proxy.image
                if (image != null) {
                    emitEvent(CameraServiceListener.OnImageAvailable(image))
                    emitEvent(CameraServiceListener.OnImageProxy(proxy))
                } else {
                    proxy.close()
                }
            } catch (e: Exception) {
                Log.d(TAG, "analyze: Error analyzing image: ${e.message}")
                proxy.close()
            }
        }
    }

    fun onDestroy() {
        Log.d(TAG, "onDestroy: Destroying CameraXService")

        try {
            // Shutdown the camera executor and unbind all use cases
            cameraExecutor?.shutdown()
            cameraProvider?.unbindAll()

            lifeCycleRegistry.currentState = Lifecycle.State.DESTROYED
            isCameraInitialized = false
            isCameraRunning = false
            serviceScope.cancel()
        } catch (e: Exception) {
            Log.d(TAG, "onDestroy: Error destroying CameraXService: ${e.message}")
        }
        emitEvent(CameraServiceListener.OnCameraDisconnected)
    }
}