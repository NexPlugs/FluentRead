package com.example.fluentread.features.onboarding

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fluentread.features.components.BuildButton
import com.example.fluentread.utils.PERMISSION_INTRODUCTION_DESC
import com.example.fluentread.utils.PERMISSION_INTRODUCTION_TITLE


/**
 * Entry route for the Enable Permission feature.
 *
 * @param modifier The [Modifier] to be applied to this route.
 * @param onNextPage The callback to be invoked when the user has completed enabling permissions.
 */
@Composable
fun EnablePermissionRoute(
    modifier: Modifier = Modifier,
    onNextPage: () -> Unit = {},
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState = onboardingViewModel.uiState.collectAsState().value

    // Access the current context if needed
    val context = LocalContext.current

    // Access the current activity if needed
    val activity = LocalActivity.current

    // Access the current window info to check if the window is focused
    val windowInfo = LocalWindowInfo.current

    LaunchedEffect(windowInfo) {
        snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused) {
                onboardingViewModel.funGetPermissionGranted(context)
            }
        }
    }

    LaunchedEffect(true) {
        onboardingViewModel.funGetPermissionGranted(context)
    }

    EnablePermissionScreen(
        modifier = modifier,
        isAccessibilityGranted = uiState.isAccessibilityGranted,
        isCameraGranted = uiState.isCameraPermissionGranted,
        onGrantedAccessibility = {
            if(activity != null) {
                onboardingViewModel.grantedAccessibility(activity)
            }
        },
        onGrantedCamera = {
            if(activity != null) {
                onboardingViewModel.grantedCameraPermission(activity)
            }
        },
        onNextPage = onNextPage
    )
}

/**
 * A simple enable permission screen composable.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 * @param isAccessibilityGranted A flag indicating if the accessibility permission is granted.
 * @param isCameraGranted A flag indicating if the camera permission is granted.
 * @param onGrantedAccessibility The callback to be invoked when the user wants to grant accessibility permission.
 * @param onGrantedCamera The callback to be invoked when the user wants to grant camera permission.
 */
@Composable
fun EnablePermissionScreen(
    modifier: Modifier = Modifier,
    isAccessibilityGranted: Boolean = false,
    isCameraGranted: Boolean = false,
    onGrantedAccessibility: () -> Unit = {},
    onGrantedCamera: () -> Unit = {},
    onNextPage: () -> Unit = { }
) {

    val permissionModifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)


    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            PERMISSION_INTRODUCTION_TITLE,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = PERMISSION_INTRODUCTION_DESC,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        PermissionField(
            modifier = permissionModifier,
            title = "Accessibility",
            description = "To monitor screen content for scrolling.",
            isEnabled = isAccessibilityGranted,
            icon = Icons.Default.Person,
            onTap = onGrantedAccessibility
        )
        PermissionField(
            modifier = permissionModifier,
            title = "Camera",
            description = "To track your eye movements.",
            isEnabled = isCameraGranted,
            icon = Icons.Default.Phone,
            onTap = onGrantedCamera
        )
        Spacer(modifier = Modifier.weight(1f))
        BuildButton(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.primary,
            onPress =  onNextPage,
            enableWidth = false,
            height = 52.dp,
            content = {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            },
            radius = 12f
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}


/**
 * A composable representing a permission field with an icon, title, description, and enabled state.
 */
@Composable
private fun PermissionField(
    modifier: Modifier,
    title: String,
    description: String,
    isEnabled: Boolean,
    icon: ImageVector,
    onTap: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isEnabled,
                onClick = onTap
            )
            .background(
                MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.1f
                    ),
                    shape = RoundedCornerShape(40.dp)
                )
            )  {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp).size(24.dp)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isEnabled) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}
