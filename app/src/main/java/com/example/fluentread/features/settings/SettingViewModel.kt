package com.example.fluentread.features.settings

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fluentread.cache.DatabaseSettingRepository
import com.example.fluentread.cache.Setting
import com.example.fluentread.permissions.AppPermissions
import com.example.fluentread.service.AppControllerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the UI state of the Settings screen.
 */
data class SettingUiState(
    val setting: Setting? = null,
    val distanceDuration: Double = 0.0,
    val delayScroll: Double = 0.0,
    val eyeTrackingSensitivity: Float = 1f,
    val isEyeTrackingEnabled: Boolean = false,
    val isAccessibilityServiceRunning: Boolean = false
)

/**
 * ViewModel responsible for managing settings data and permissions.
 */
@HiltViewModel
class SettingViewModel @Inject constructor(
    private val databaseSettingRepository: DatabaseSettingRepository,
    private val appControllerRepository: AppControllerRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val appPermissions = AppPermissions.getInstance()

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState

    init {
        loadSettings()
    }

    /**
     * Loads current settings from the local database.
     * Falls back to default if no settings are found.
     */
    private fun loadSettings() = viewModelScope.launch {
        runCatching {
            databaseSettingRepository.getSetting()
        }.onSuccess { settings ->
            val setting = settings ?: Setting(distanceDuration = 1.0, delayScroll = 1.0, key = "default")
            _uiState.update {
                it.copy(
                    setting = setting,
                    distanceDuration = setting.distanceDuration,
                    delayScroll = setting.delayScroll
                )
            }
        }.onFailure { e ->
            Log.e("SettingViewModel", "Failed to load settings", e)
        }
    }

    /**
     * Updates the current settings both in memory and in the local database.
     */
    fun updateSettings(
        distanceDuration: Double? = null,
        delayScroll: Double? = null,
        eyeTrackingSensitivity: Float? = null
    ) = viewModelScope.launch {
        _uiState.value.setting?.let { current ->
            val updated = current.copy(
                distanceDuration = distanceDuration ?: current.distanceDuration,
                delayScroll = delayScroll ?: current.delayScroll
            )
            databaseSettingRepository.insertSetting(updated)

            _uiState.update {
                it.copy(
                    setting = updated,
                    distanceDuration = updated.distanceDuration,
                    delayScroll = updated.delayScroll,
                    eyeTrackingSensitivity = eyeTrackingSensitivity ?: it.eyeTrackingSensitivity
                )
            }
        }
    }

    /**
     * Checks whether the Accessibility Service and Eye Tracking features are active.
     */
    fun refreshServiceStatus() = viewModelScope.launch {
        val isRunning = appPermissions.isAccessibilityPermissionGranted(context)
        val isEyeTrackingEnabled = appControllerRepository.cameraIsRunning
        _uiState.update {
            it.copy(
                isAccessibilityServiceRunning = isRunning,
                isEyeTrackingEnabled = isEyeTrackingEnabled
            )
        }
    }

    fun isAccessibilityPermissionGranted(): Boolean {
        return appPermissions.isAccessibilityPermissionGranted(context)
    }

    /**
     * Triggers the system dialog for granting Accessibility Service permission.
     */
    fun requestAccessibilityPermission(activity: Activity) {
        appPermissions.requestAccessibilityPermission(activity)
    }
}
