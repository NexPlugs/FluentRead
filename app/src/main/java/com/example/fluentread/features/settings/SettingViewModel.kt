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

private const val TAG = "SettingViewModel"

/**
 * UI state for the Settings screen.
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
 * ViewModel for managing settings and permissions.
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
     * Loads settings from the database, falling back to defaults if necessary.
     */
    private fun loadSettings() = viewModelScope.launch {
        runCatching { databaseSettingRepository.getSetting() }
            .onSuccess { settings ->
                val setting = settings ?: Setting(distanceDuration = 1.0, delayScroll = 1.0, key = "default")
                _uiState.update {
                    it.copy(
                        setting = setting,
                        distanceDuration = setting.distanceDuration,
                        delayScroll = setting.delayScroll
                    )
                }
            }
            .onFailure { e ->
                Log.e(TAG, "Failed to load settings", e)
            }
    }

    /**
     * Updates settings in memory and persists to the database.
     */
    fun updateSettings(
        distanceDuration: Double? = null,
        delayScroll: Double? = null,
        eyeTrackingSensitivity: Float? = null
    ) = viewModelScope.launch {
        val current = _uiState.value.setting ?: return@launch
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

    /**
     * Refreshes the status of accessibility and eye tracking services.
     */
    fun refreshServiceStatus() = viewModelScope.launch {
        val isAccessibilityRunning = appPermissions.isAccessibilityPermissionGranted(context)
        val isEyeTrackingActive = appControllerRepository.cameraIsRunning
        _uiState.update {
            it.copy(
                isAccessibilityServiceRunning = isAccessibilityRunning,
                isEyeTrackingEnabled = isEyeTrackingActive
            )
        }
    }

    /**
     * Returns true if accessibility permission is granted.
     */
    fun isAccessibilityPermissionGranted(): Boolean =
        appPermissions.isAccessibilityPermissionGranted(context)

    /**
     * Requests accessibility permission via system dialog.
     */
    fun requestAccessibilityPermission(activity: Activity) {
        appPermissions.requestAccessibilityPermission(activity)
    }
}