package com.example.fluentread.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

    val itemModifier = Modifier.padding(horizontal = 16.dp)

    Scaffold(
        modifier = modifier,
        topBar = {
            //
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(
                    top = 32.dp,
                    start = 16.dp,
                    bottom = 12.dp
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Eye Tracking",
                style = headerStyle,
                modifier = itemModifier
            )

            Spacer(modifier = Modifier.height(8.dp))

            EyeTrackingField()

            Spacer(modifier = Modifier.height(16.dp))


            // More settings fields can be added here
        }

    }
}


@Composable
private fun EyeTrackingField() {

}