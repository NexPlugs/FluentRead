package com.example.fluentread.features.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fluentread.cache.DatabaseSettingRepository
import com.example.fluentread.cache.Setting
import com.example.fluentread.permissions.AppPermissions
import com.example.fluentread.service.AppController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * UI state for the Setting screen.
 * Holds the current settings values.
 */
data class SettingUiState(
    val distanceDuration: Double = 0.0,
    val delayScroll: Double = 0.0,
    val setting: Setting? = null,
    val isEyeTrackingEnabled: Boolean = false,
    val eyeTrackingSensitivity: Float = 1f,
    val isAccessibilityServiceRunning: Boolean = false
)


/**
 * ViewModel for the Setting screen.
 * Currently, it does not hold any state or logic.
 */

@HiltViewModel
class SettingViewModel @Inject constructor(
    val databaseSettingRepository: DatabaseSettingRepository,
): ViewModel() {

    private val appPermissions: AppPermissions = AppPermissions.getInstance()

    @SuppressLint("StaticFieldLeak")
    val appController: AppController? = AppController.getInstance()

    private val _settingUiState: MutableStateFlow<SettingUiState> = MutableStateFlow<SettingUiState>(SettingUiState())

    val uiState: StateFlow<SettingUiState> = _settingUiState

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = databaseSettingRepository.getSetting()
                if (settings != null) {
                    _settingUiState.update {
                        it.copy(
                            setting = settings,
                            distanceDuration = settings.distanceDuration,
                            delayScroll = settings.delayScroll
                        )
                    }
                } else {
                    // fallback to default
                    _settingUiState.update {
                        it.copy(
                            setting = Setting(distanceDuration = 1.0, delayScroll = 1.0, key = "default")
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingVM", "Failed to load settings", e)
            }
        }
    }

    fun getAccessibilityServiceStatus(context: Context) {
        viewModelScope.launch {
            val isRunning = appPermissions.isAccessibilityPermissionGranted(context)
            val isEyeTrackingEnabled = appController?.isEyeTracking == true
            _settingUiState.update {
                it.copy(isAccessibilityServiceRunning = isRunning, isEyeTrackingEnabled = isEyeTrackingEnabled)
            }
        }
    }

    fun grantedAccessibilityService(activity: Activity) {
        appPermissions.requestAccessibilityPermission(activity)
    }


    fun updateSettings(
        distanceDuration: Double? = null,
        delayScroll: Double? = null,
        eyeTrackingSensitivity: Float? = null
    ) {
        viewModelScope.launch {
            val currentSetting = _settingUiState.value.setting
            if (currentSetting != null) {
                val updatedSetting = currentSetting.copy(
                    distanceDuration = distanceDuration ?: currentSetting.distanceDuration,
                    delayScroll = delayScroll ?: currentSetting.delayScroll

                )
                databaseSettingRepository.insertSetting(updatedSetting)
                _settingUiState.update {
                    it.copy(
                        setting = updatedSetting,
                        distanceDuration = updatedSetting.distanceDuration,
                        delayScroll = updatedSetting.delayScroll,
                        eyeTrackingSensitivity = eyeTrackingSensitivity ?: it.eyeTrackingSensitivity
                    )
                }
            }
        }
    }
}