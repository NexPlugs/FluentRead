package com.example.fluentread.features.settings

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

private val CardShape = RoundedCornerShape(16.dp)
private val SectionSpacing = 16.dp
private val FieldPadding = 16.dp
private val DividerSpacing = 12.dp
private val ButtonHeight = 52.dp

@Composable
fun SettingRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val windowInfo = LocalWindowInfo.current

    LaunchedEffect(windowInfo) {
        snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused) viewModel.refreshServiceStatus()
        }
    }
    LaunchedEffect(true) { viewModel.refreshServiceStatus() }

    SettingScreen(
        modifier = modifier,
        uiState = uiState,
        onEyeTrackingToggled = { if (it) context.launchAppController() },
        onSensitivityChanged = { viewModel.updateSettings(eyeTrackingSensitivity = it) },
        onGrantedPermissions = { activity?.let { viewModel.requestAccessibilityPermission(it) } }
    )
}

/**
 * Main settings screen composable.
 */
@Composable
private fun SettingScreen(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    onEyeTrackingToggled: (Boolean) -> Unit,
    onSensitivityChanged: (Float) -> Unit,
    onGrantedPermissions: () -> Unit
) {
    val fieldModifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface, shape = CardShape)
        .padding(FieldPadding)

    Scaffold(
        modifier = modifier,
        topBar = { SettingsTopBar() }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = FieldPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SectionSpacing)
        ) {
            HorizontalDivider()
            SectionHeader("Eye Tracking")
            EyeTrackingCard(
                modifier = fieldModifier,
                enabled = uiState.isEyeTrackingEnabled,
                sensitivity = uiState.eyeTrackingSensitivity,
                onToggle = onEyeTrackingToggled,
                onSensitivityChange = onSensitivityChanged
            )
            Spacer(Modifier.height(4.dp))
            SectionHeader("Permissions")
            PermissionsField(
                modifier = fieldModifier,
                isEnabled = uiState.isAccessibilityServiceRunning,
                onGrantedPermissions = onGrantedPermissions
            )
            Spacer(Modifier.height(4.dp))
            SectionHeader("General")
            GeneralField(modifier = fieldModifier)
        }
    }
}

@Composable
private fun SettingsTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            modifier = Modifier.padding(16.dp).size(24.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
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
                    "Eye Tracking",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Enable or disable eye tracking for auto-scrolling.",
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
            "Sensitivity",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            "Adjust the sensitivity of the eye tracking feature.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(Modifier.height(4.dp))
        Slider(
            value = sensitivity,
            onValueChange = onSensitivityChange,
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
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun PermissionsField(
    modifier: Modifier,
    isEnabled: Boolean,
    onGrantedPermissions: () -> Unit
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
                Spacer(Modifier.height(4.dp))
                Text(
                    "Grant accessibility permission to enable eye tracking functionality.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Spacer(Modifier.width(8.dp))
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
        Spacer(Modifier.height(16.dp))
        if (!isEnabled)
            BuildButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary,
                onPress = onGrantedPermissions,
                enableWidth = false,
                height = ButtonHeight,
                content = {
                    Text(
                        "Grant Permission",
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
                Spacer(Modifier.height(DividerSpacing))
                HorizontalDivider()
                Spacer(Modifier.height(DividerSpacing))
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

