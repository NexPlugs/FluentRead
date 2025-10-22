package com.example.fluentread.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun SettingRoute(
    modifier: Modifier,
    settingViewModel: SettingViewModel = hiltViewModel<SettingViewModel>()
) {
    val uiState = settingViewModel.uiState.collectAsState().value

    SettingScreen(modifier = modifier)
}


@Composable
private fun SettingScreen(modifier: Modifier) {
    val headerStyle = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold
    )

    val itemModifier = Modifier.padding(horizontal = 12.dp)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Settings",
            style = headerStyle,
            modifier = itemModifier
        )
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Text(
            "Eye Tracking",
            style = headerStyle,
            modifier = itemModifier
        )
    }
}


@Composable
private fun EyeTrackingField() {

}