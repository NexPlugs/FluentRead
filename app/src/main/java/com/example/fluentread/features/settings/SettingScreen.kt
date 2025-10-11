package com.example.fluentread.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun SettingRoute(
    modifier: Modifier,
    settingViewModel: SettingViewModel = hiltViewModel<SettingViewModel>()
) {
    SettingScreen(modifier = modifier)
}


@Composable
fun SettingScreen(modifier: Modifier) {
    Column(modifier = modifier) {  }
}
