package com.example.fluentread.features.splash

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val SPLASH_ROUTE = "splash_route"

/**
 * Composable function to define the navigation route for the Splash Screen.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 * @param onNextPage Lambda to be invoked when navigating to the next page.
 */
fun NavGraphBuilder.splashScreen(
    modifier: Modifier,
    onNextPage: () -> Unit = { },
) {
    composable(SPLASH_ROUTE) {
        SplashRoute(modifier = modifier, onNextPage = onNextPage)
    }
}