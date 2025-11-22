package com.example.fluentread.service.camera.customize

import android.hardware.camera2.CameraCharacteristics
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider

/**
 * Object to define camera capabilities and utility functions.
 */
object CameraCapabilities {
    const val MAX_ZOOM_LEVEL = 10.0f
    const val MIN_ZOOM_LEVEL = 1.0f

    /** Checks if the provided zoom level is within valid range. */
    fun isZoomLevelValid(zoomLevel: Float): Boolean {
        return zoomLevel in MIN_ZOOM_LEVEL..MAX_ZOOM_LEVEL
    }

    /** Retrieves the camera hardware level for the selected camera. */
    @OptIn(ExperimentalCamera2Interop::class)
    fun getCameraLevel(
        cameraSelector: CameraSelector,
        cameraProvider: ProcessCameraProvider
    ): Int {
        return cameraSelector.filter(cameraProvider.availableCameraInfos).firstOrNull()
            ?.let { Camera2CameraInfo.from(it) }
            ?.getCameraCharacteristic(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            ?: CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
    }
}