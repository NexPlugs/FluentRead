package com.example.fluentread.features.settings

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable


const val SETTING_ROUTE = "setting_route"

/**
 * Composable function to define the navigation route for the Setting Screen.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 */
fun NavGraphBuilder.settingScreen(
    modifier: Modifier,
) {
    composable(SETTING_ROUTE) {
        SettingRoute(modifier = modifier)
    }
}