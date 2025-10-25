package com.example.fluentread.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Repository class for managing application controller state.
 * @constructor Injects the repository.
 *
 */
class AppControllerRepository @Inject constructor() {
    // Backing property to avoid state updates from other classes.
    private val _appData = MutableStateFlow(AppData())
    val appData: StateFlow<AppData> = _appData

    // Expose cameraIsRunning as a read-only property
    val cameraIsRunning: Boolean get() = appData.value.cameraIsRunning

    // State flow for eye tracking status
    private val _isEyeTracking = MutableStateFlow(false)
    val isEyeTracking: StateFlow<Boolean> = _isEyeTracking

    fun updateAppData(
        cameraIsRunning: Boolean
    ) {
        _appData.update {
            it.copy(
                cameraIsRunning = cameraIsRunning
            )
        }
    }

    fun updateEyeTracking(isTracking: Boolean) {
        _isEyeTracking.value = isTracking
    }

}