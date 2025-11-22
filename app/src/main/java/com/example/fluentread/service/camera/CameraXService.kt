@file:Suppress("MissingPermission", "OPT_IN_USAGE")

package com.example.fluentread.service.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object CameraXService : LifecycleOwner {

    private const val TAG = "CameraXService"

    //region === Lifecycle ===
    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    //endregion

    //region === Internal State ===
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor: ExecutorService? = null
    private var isRunning = false
    private var isInitialized = false
    //endregion

    //region === Coroutine & Events ===
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _events = MutableSharedFlow<CameraServiceListener>()
    val events: SharedFlow<CameraServiceListener> = _events.asSharedFlow()

    private fun post(event: CameraServiceListener) = scope.launch { _events.emit(event) }
    //endregion

    //region === Public Lifecycle Methods ===
    fun onCreate() = runCatching {
        Log.i(TAG, "Initializing CameraXService...")
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        cameraExecutor = Executors.newSingleThreadExecutor()
        isInitialized = true
    }.onFailure {
        Log.e(TAG, "Camera initialization failed: ${it.message}", it)
        isInitialized = false
    }

    fun onStartCameraX(context: Context) {
        if (!isInitialized) {
            Log.w(TAG, "CameraXService not initialized, call onCreate() first.")
            return
        }
        if (isRunning) {
            Log.d(TAG, "Camera already running, ignoring start request.")
            return
        }

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        Log.d(TAG, "Starting CameraX...")

        runCatching {
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener(
                {
                   runCatching {
                       cameraProvider = future.get()
                       bindUseCase(cameraProvider)
                       post(CameraServiceListener.OnCameraOpened)
                   }.onFailure {
                       Log.e(TAG, "Error during CameraX start: ${it.message}", it)
                   }
                },
                ContextCompat.getMainExecutor(context)
            )
        }.onFailure {
            Log.e(TAG, "Failed to start CameraX: ${it.message}", it)
            post(CameraServiceListener.OnCameraError(it.message ?: "Unknown error"))
        }
    }

    fun onStopCameraX() {
        if (!isRunning) {
            Log.d(TAG, "Camera is not running, nothing to stop.")
            return
        }

        Log.i(TAG, "Stopping CameraX...")
        runCatching {
            cameraProvider?.unbindAll()
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
            isRunning = false
            post(CameraServiceListener.OnCameraDisconnected)
        }.onFailure {
            Log.e(TAG, "Error stopping CameraX: ${it.message}", it)
        }
    }

    fun onDestroy() {
        Log.i(TAG, "Destroying CameraXService...")
        runCatching {
            cameraProvider?.unbindAll()
            cameraExecutor?.shutdown()
            scope.cancel()
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            isRunning = false
            isInitialized = false
        }.onFailure {
            Log.e(TAG, "Error destroying CameraXService: ${it.message}", it)
        }
        post(CameraServiceListener.OnCameraDisconnected)
    }
    //endregion

    //region === Camera Binding & Analyzer ===
    private fun bindUseCase(provider: ProcessCameraProvider?) {
        if(provider == null) {
            Log.e(TAG, "Camera provider is null, cannot bind use case.")
            post(CameraServiceListener.OnCameraError("Camera provider is null"))
            return
        }
        val executor = cameraExecutor ?: return
        val selector = CameraSelector.DEFAULT_FRONT_CAMERA

        provider.unbindAll()

        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(executor, FaceAnalyzer())
            }

        runCatching {
            if (!provider.hasCamera(selector)) {
                throw IllegalStateException("No front camera available.")
            }
            provider.bindToLifecycle(this, selector, analyzer)
            isRunning = true
            post(CameraServiceListener.OnCameraOpened)
        }.onFailure {
            Log.e(TAG, "Failed to bind use case: ${it.message}", it)
            post(CameraServiceListener.OnCameraError(it.message ?: "Binding error"))
            onStopCameraX()
        }
    }

    fun openFlash() = runCatching {
        val provider = cameraProvider ?: return@runCatching null
        provider.unbindAll()
        val camera = provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA)
        camera.cameraControl.enableTorch(true)
    }.onFailure {
        Log.e(TAG, "Failed to open flash: ${it.message}", it)
    }
    //endregion

    //region === Image Analyzer ===
    private class FaceAnalyzer : ImageAnalysis.Analyzer {
        @ExperimentalGetImage
        override fun analyze(proxy: ImageProxy) {
            try {
                proxy.image?.let { image ->
                    post(CameraServiceListener.OnImageAvailable(image))
                    post(CameraServiceListener.OnImageProxy(proxy))
                } ?: proxy.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing image: ${e.message}", e)
                proxy.close()
            }
        }
    }
    //endregion
}
