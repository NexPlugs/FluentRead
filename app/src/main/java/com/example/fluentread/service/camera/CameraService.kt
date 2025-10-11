package com.example.fluentread.service.camera

import android.app.Service
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import com.example.fluentread.permissions.AppPermissions

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

    // Instance of AppPermissions to handle permission-related tasks
    private val appPermission: AppPermissions = AppPermissions.getInstance()


    //Background thread and handler for camera operations
    private val handler: Handler? = null
    private val thread: HandlerThread? = null

    companion object {
        const val TAG = "CameraService"
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
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

            //Add Background thread for camera operations
            thread ?: HandlerThread(TAG).apply {
                start()
            }
            handler ?: Handler(thread!!.looper)

            Log.d(TAG, "onCreate: Camera Service created with cameraId: $cameraId")

            isServiceInitialized = true
        } catch (e: Exception) {
            Log.d(TAG, "onCreate: ${e.message}")
            isServiceInitialized = false
        }
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
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.d(TAG, "onDisconnected: Camera disconnected")
                    closeCamera()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.d(TAG, "onError: Error opening camera - $error")
                    closeCamera()
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
     * Called when the service is destroyed.
     * Cleans up resources and closes the camera if it's open.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Camera Service destroyed")
        //Turn off camera if it's on
        cameraDevice?.close()
        cameraDevice = null

        //Stop background thread
        thread?.quitSafely()
        Log.d(TAG, "onDestroy: Camera Service resources cleaned up")
    }


}

