package com.example.fluentread.features.settings

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fluentread.features.components.BuildButton
import com.example.fluentread.features.settings.utils.GeneralAction
import com.example.fluentread.utils.launchAppController

@Composable
fun SettingRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Access the current context if needed
    val context = LocalContext.current

    // Access the current activity if needed
    val activity = LocalActivity.current

    // Access the current window info to check if the window is focused
    val windowInfo = LocalWindowInfo.current

    LaunchedEffect(windowInfo) {
        snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused) {
                viewModel.getAccessibilityServiceStatus(context)
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.getAccessibilityServiceStatus(context)
    }


    SettingScreen(
        modifier = modifier,
        uiState = uiState,
        onEyeTrackingToggled = {
            if(it) {
                context.launchAppController()
            }
        },
        onSensitivityChanged = {
            viewModel.updateSettings(eyeTrackingSensitivity = it)
        },
        onGrantedPermissions = {
            if(activity == null) return@SettingScreen
            viewModel.grantedAccessibilityService(activity)
        }
    )
}

@Composable
private fun SettingScreen(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    onEyeTrackingToggled: (Boolean) -> Unit,
    onSensitivityChanged: (Float) -> Unit,
    onGrantedPermissions: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    val headerStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    val fieldModifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface, shape = cardShape)
        .padding(16.dp)

    Scaffold(
        modifier = modifier,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Back",
                    modifier = Modifier.padding(16.dp).size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HorizontalDivider()
            SectionHeader(text = "Eye Tracking")
            EyeTrackingCard(
                modifier = fieldModifier,
                enabled = uiState.isEyeTrackingEnabled,
                sensitivity = uiState.eyeTrackingSensitivity,
                onToggle = onEyeTrackingToggled,
                onSensitivityChange = onSensitivityChanged
            )
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(text = "Permissions")
            PermissionsField(
                modifier = fieldModifier,
                onGrantedPermissions = onGrantedPermissions,
                isEnabled = uiState.isAccessibilityServiceRunning
            )
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(text = "General")
            GeneralField(modifier = fieldModifier)
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EyeTrackingCard(
    modifier: Modifier,
    enabled: Boolean,
    sensitivity: Float,
    onToggle: (Boolean) -> Unit,
    onSensitivityChange: (Float) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Eye Tracking",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enable or disable eye tracking for auto-scrolling.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            )
        }
        HorizontalDivider()
        Text(
            text = "Sensitivity",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Adjust the sensitivity of the eye tracking feature.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = sensitivity,
            onValueChange = {
                onSensitivityChange(it)
            },
            valueRange = 0f..10f,
            modifier = Modifier.height(2.dp),
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier
                        .height(10.dp)
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .clip(CircleShape),
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary,
                    )
                )
            },
            thumb = {}
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun PermissionsField(
    modifier: Modifier,
    isEnabled: Boolean,
    onGrantedPermissions: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Accessibility",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Grant accessibility permission to enable eye tracking functionality.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isEnabled) Color.Green.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isEnabled) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) Color.Green
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if(!isEnabled)
            BuildButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary,
                onPress = onGrantedPermissions,
                enableWidth = false,
                height = 52.dp,
                content = {
                    Text(
                        text = "Grant Permission",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                },
                radius = 12f
            )
    }
}

@Composable
private fun GeneralField(modifier: Modifier) {
    Column(modifier) {
        GeneralAction.entries.forEach {
            GeneralSettingItem(action = it)
            if (it != GeneralAction.entries.last()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun GeneralSettingItem(action: GeneralAction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = action.getTitle(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    MaterialTheme {
        SettingScreen(
            uiState = SettingUiState(
                isEyeTrackingEnabled = true,
                eyeTrackingSensitivity = 0.7f
            ),
            onEyeTrackingToggled = {},
            onSensitivityChanged = {},
            onGrantedPermissions = {},
        )
    }
}