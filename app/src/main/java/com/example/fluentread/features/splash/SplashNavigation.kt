package com.example.fluentread.features.splash

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import cafe.adriel.voyager.core.screen.Screen

const val SPLASH_ROUTE = "splash_route"

/**
 * Extension function to add the Splash Screen to the [NavGraphBuilder].
 *
 * @param modifier The [Modifier] to be applied to the layout.
 * @param onNextPage Lambda to be invoked when navigating to the next page.
 */
class Splash(
    val modifier: Modifier,
    val onNextPage: () -> Unit = { },
): Screen {

    @Composable
    override fun Content() {
        SplashRoute( modifier = modifier, onNextPage = onNextPage)
    }
}