package com.example.fluentread.service.camera

import android.media.Image
import androidx.camera.core.ImageProxy

/**
 * Camera service listener for camera events
 * - OnCameraOpened: Camera is opened
 * - OnCameraDisconnected: Camera is disconnected
 * - OnCameraError: Camera error occurred
 * - OnImageAvailable: Image is available
 * - OnImageProxy: ImageProxy is available
 */
sealed class CameraServiceListener {
    object OnCameraOpened: CameraServiceListener()
    object OnCameraDisconnected: CameraServiceListener()
    data class OnCameraError(val error: String): CameraServiceListener()
    data class OnImageAvailable(val image: Image): CameraServiceListener()
    data class OnImageProxy(val imageProxy:ImageProxy): CameraServiceListener()
}
