package com.example.fluentread.service.camera

import android.app.Service
import android.content.Intent
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import com.example.fluentread.permissions.AppPermissions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow


/**
 * A foreground service that handles camera operations.
 * This service is designed to run in the foreground to ensure it remains active
 * while performing camera-related tasks.
 */
class CameraService: Service() {
    //CameraManager to manage camera operations
    private var cameraManager: CameraManager? = null

    // Camera ID and CameraDevice to handle specific camera
    private var cameraId: String? = null

    //CameraDevice to represent the opened camera
    private var cameraDevice: CameraDevice? = null

    // A flag to check if the service is initialized
    private var isServiceInitialized = false

    // ImageReader to handle image capture (if needed in future)
    private var imageReader: ImageReader? = null

    // Instance of AppPermissions to handle permission-related tasks
    private val appPermission: AppPermissions = AppPermissions.getInstance()


    //Background thread and handler for camera operations
    private var handler: Handler? = null
    private var thread: HandlerThread? = null

    //Listener
    var cameraServiceListener: CameraServiceListener? = null

    companion object {
        const val TAG = "CameraService"

        // Instance of CameraService
        var INSTANCE: CameraService? = null

        fun getInstance(): CameraService {
            if (INSTANCE == null) {
                return CameraService()
            }
            return INSTANCE!!
        }
    }

    init {
        INSTANCE = this
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    /**
     * Interface for listening to camera service events.
     * Implement this interface to receive callbacks for camera events such as opening, disconnection, and errors.
     */
    private val _listener: MutableSharedFlow<CameraServiceListener> = MutableSharedFlow<CameraServiceListener>(replay = 0)

    val listener: SharedFlow<CameraServiceListener> get() = _listener

    // Expose the listener as a SharedFlow to allow external components to collect events
    private fun emit(event: CameraServiceListener) {
        _listener.tryEmit(event)
    }

    // Function to get the listener SharedFlow



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Camera Service started")
        // If the service is not initialized, stop the service
        if(!isServiceInitialized) {
            Log.d(TAG, "onStartCommand: Service not initialized. Stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

        // Open the camera
        openCamera()
        // If the service is killed by the system, do not recreate it
        return START_NOT_STICKY
    }

    /**
     * Called when the service is created.
     * Initializes the CameraManager and retrieves the camera ID.
     */
    override fun onCreate() {
        super.onCreate()

        //Check if camera permission is granted
        if(!appPermission.isCameraPermissionGranted(this)) {
            Log.d(TAG, "onCreate: Camera permission not granted. Stopping service.")
            stopSelf()
            return
        }

        try {
            cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            cameraId = getCameraDevice()

            startBackgroundThread()

            // ----------------------------------
            setupImageReader()

            Log.d(TAG, "onCreate: Camera Service created with cameraId: $cameraId")

            isServiceInitialized = true
        } catch (e: Exception) {
            Log.d(TAG, "onCreate: ${e.message}")
            isServiceInitialized = false
        }
    }

    /**
     * Sets up the ImageReader to capture images from the camera.
     * This method initializes the ImageReader with the desired resolution and format,
     */
    private fun setupImageReader() {
        imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.YUV_420_888, 2).apply {
            setOnImageAvailableListener({ reader ->
                reader.acquireLatestImage()?.let { image ->

                    //Sink the image to the listener
                    emit(CameraServiceListener.OnImageAvailable(image))
                    image.close()
                }
            }, handler)
        }
    }

    /**
     * Starts a background thread and its associated handler for camera operations.
     * This ensures that camera operations do not block the main UI thread.
     */
    private fun startBackgroundThread() {
        thread ?: HandlerThread(TAG).apply {
            start()
        }
        handler ?: Handler(thread!!.looper)
    }

    /**
     * Retrieves the ID of the back-facing camera.
     * @return The camera ID of the back-facing camera, or null if not found or an error occurs.
     */
    private fun getCameraDevice(): String? {
        if(cameraManager == null) return null
        try {
            for(id in cameraManager!!.cameraIdList) {

                logCameraInformation(id)

                // Get the camera characteristics for each camera
                val characteristics = cameraManager!!.getCameraCharacteristics(id)

                // Check if the camera is a back-facing camera
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                // If it's back-facing, return its ID
                if(facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id
                    return cameraId
                }
            }

        } catch (e: Exception) {
            Log.d(TAG, "getCameraDevice: ${e.message}")
            return null
        }
        return null
    }

    /**
     * Opens the camera using the CameraManager.
     * Ensures the service is initialized and the camera ID is valid before attempting to open the camera.
     */
    fun openCamera() {

        // If the service is not initialized or cameraId is null, log and return
        if(!isServiceInitialized) {
            Log.d(TAG, "openCamera: Service not initialized")
            return
        }

        // Check if cameraId and cameraManager are not null
        if(cameraId == null || cameraManager == null) {
            Log.d(TAG, "openCamera: Camera ID or Camera Manager is null")
            return
        }

        // Check if  thread is null
        if(thread == null || handler == null) {
            Log.d(TAG, "openCamera: Background thread or handler is null")
            return
        }

        try {
            cameraManager!!.openCamera(cameraId!!, object: CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d(TAG, "onOpened: Camera opened successfully")
                    cameraDevice = camera
                    emit(CameraServiceListener.OnCameraOpened)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.d(TAG, "onDisconnected: Camera disconnected")
                    closeCamera()
                    emit(CameraServiceListener.OnCameraDisconnected)
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.d(TAG, "onError: Error opening camera - $error")
                    closeCamera()
                    emit(CameraServiceListener.OnCameraError("Error code: $error") )
                }
            }, handler)
        } catch (e: SecurityException) {
            Log.d(TAG, "openCamera: SecurityException - ${e.message}")
        } catch (e: Exception) {
            Log.d(TAG, "openCamera: Exception - ${e.message}")
        }
    }

    private fun closeCamera() {
        cameraDevice?.close()
        cameraDevice = null
    }

    /**
     * Logs detailed information about the specified camera.
     * @param id The ID of the camera to log information for.
     */
    private fun logCameraInformation(id: String) {
        //Check is debug mode

        if(cameraManager == null) return
        try {
            val characteristics = cameraManager!!.getCameraCharacteristics(id)
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
            val capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)

            Log.d(TAG, "Camera ID: $id")
            Log.d(TAG, "Lens Facing: $lensFacing")
            Log.d(TAG, "Sensor Orientation: $sensorOrientation")
            Log.d(TAG, "Capabilities: ${capabilities?.joinToString(", ")}")

        } catch (e: Exception) {
            Log.d(TAG, "logCameraInformation: ${e.message}")
        }
    }

    /**
     * Stops the background thread and its associated handler.
     * This is important to free up resources and prevent memory leaks.
     */
    private fun stopBackgroundThread() {
        thread?.quitSafely()
        try {
            thread?.join()
        } catch (e: InterruptedException) {
            Log.e(TAG, "stopBackgroundThread: ${e.message}", e)
        } finally {
            thread = null
            handler = null
        }
        Log.d(TAG, "stopBackgroundThread: Background thread stopped")
    }

    /**
     * Called when the service is destroyed.
     * Cleans up resources and closes the camera if it's open.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Camera Service destroyed")
        //Turn off camera if it's on
        cameraDevice?.close()
        cameraDevice = null

        //Close ImageReader
        imageReader?.close()
        imageReader = null

        //Remove listener
        cameraServiceListener = null

        //Stop background thread
        stopBackgroundThread()
    }
}

