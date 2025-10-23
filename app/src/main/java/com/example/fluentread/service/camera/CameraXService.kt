@file:Suppress("MissingPermission", "OPT_IN_USAGE")

package com.example.fluentread.service.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.fluentread.utils.launchWithMutex
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _events = MutableSharedFlow<CameraServiceListener>()
    val events: SharedFlow<CameraServiceListener> = _events.asSharedFlow()
    private val mutex: Mutex = Mutex()

    private fun post(event: CameraServiceListener) = scope.launch { _events.emit(event) }
    //endregion

    //region === Public Lifecycle Methods ===
    fun onCreate(context: Context) = runCatching {
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

        scope.launchWithMutex(mutex) {
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            Log.d(TAG, "Starting CameraX...")

            runCatching {
                val provider = context.getCameraProvider()
                cameraProvider = provider
                bindUseCase(provider)
                post(CameraServiceListener.OnCameraOpened)
            }.onFailure {
                Log.e(TAG, "Failed to start CameraX: ${it.message}", it)
                post(CameraServiceListener.OnCameraError(it.message ?: "Unknown error"))
            }
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
    private fun bindUseCase(provider: ProcessCameraProvider) {
        val executor = cameraExecutor ?: return
        val selector = CameraSelector.DEFAULT_FRONT_CAMERA

        provider.unbindAll()

        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(executor, FaceAnalyzer())
            }

        runCatching {
            if (!provider.hasCamera(selector)) {
                throw IllegalStateException("No front camera available.")
            }
            provider.bindToLifecycle(this, selector, analyzer)
            isRunning = true
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

//region === Extension Utilities ===
private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            runCatching { cont.resume(future.get()) }
                .onFailure { cont.resumeWithException(it) }
        }, ContextCompat.getMainExecutor(this))
    }
//endregion
