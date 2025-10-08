package com.example.fluentread.features.onboarding

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable


const val ONBOARDING_ROUTE = "onboarding_route"

const val ENABLE_PERMISSION_ROUTE = "enable_permission_route"

/**
 * Composable function to define the navigation route for the Onboarding Screen.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 * @param onFinishOnboarding Lambda to be invoked when finishing the onboarding process.
 */
fun NavGraphBuilder.onboardingScreen(
    modifier: Modifier,
    onFinishOnboarding: () -> Unit
) {
    composable(ONBOARDING_ROUTE) {
        OnboardingRoute(
            modifier =  modifier,
            onNextPage = onFinishOnboarding,
        )
    }
}


/**
 * Composable function to define the navigation route for the Enable Permission Screen.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 * @param onFinishEnablingPermissions Lambda to be invoked when finishing enabling permissions.
 */
fun NavGraphBuilder.enablePermissionScreen(
    modifier: Modifier,
    onFinishEnablingPermissions: () -> Unit = {}
) {
    composable(ENABLE_PERMISSION_ROUTE) {
        EnablePermissionRoute(
            modifier = modifier,
            onNextPage = onFinishEnablingPermissions
        )
    }
}