package com.example.fluentread.features.onboarding
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fluentread.permissions.AppPermissions
import com.example.fluentread.service.camera.CameraService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing the UI state for the Onboarding process.
 *
 * @property isAccessibilityGranted Boolean indicating if accessibility permission is granted.
 * @property isCameraPermissionGranted Boolean indicating if camera permission is granted.
 */
data class OnboardingUiState(
    val isAccessibilityGranted: Boolean = false,
    val isCameraPermissionGranted: Boolean = false
)


/**
 * ViewModel for managing the state and logic of the Onboarding process.
 *
 * @property appPermissions Instance of [AppPermissions] to handle permission-related tasks.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(): ViewModel() {
    private val appPermissions: AppPermissions = AppPermissions.getInstance()

    // Backing property to avoid state updates from other classes.
    private val _uiState: MutableStateFlow<OnboardingUiState> = MutableStateFlow<OnboardingUiState>(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /**
     * Function to check and update the permission states.
     *
     * @param context The context to use for checking permissions.
     */
    fun funGetPermissionGranted(context: Context) {
        viewModelScope.launch {
            val isAccessibilityGranted = appPermissions.isAccessibilityPermissionGranted(context)
            val isCameraPermissionGranted = appPermissions.isCameraPermissionGranted(context)

            // Start the camera service if camera permission is granted
            if(isCameraPermissionGranted) {
                val intent = Intent(context, CameraService::class.java)
                context.startService(intent)
            }

            _uiState.update {
                it.copy(
                    isAccessibilityGranted = isAccessibilityGranted,
                    isCameraPermissionGranted = isCameraPermissionGranted
                )
            }
        }
    }

    // Functions to request permissions
    fun grantedAccessibility(activity: Activity) {
        appPermissions.requestAccessibilityPermission(activity)
    }

    // Function to request camera permission
    fun grantedCameraPermission(activity: Activity) {
        appPermissions.requestCameraPermission(activity)
    }

}