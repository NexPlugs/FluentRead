package com.example.fluentread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.fluentread.features.onboarding.ENABLE_PERMISSION_ROUTE
import com.example.fluentread.features.onboarding.ONBOARDING_ROUTE
import com.example.fluentread.features.onboarding.enablePermissionScreen
import com.example.fluentread.features.onboarding.onboardingScreen
import com.example.fluentread.features.settings.settingScreen
import com.example.fluentread.features.splash.SPLASH_ROUTE
import com.example.fluentread.features.splash.splashScreen
import com.example.fluentread.service.AppController

/**
 * App Navigation this is the entry point for navigation
 */

@Composable
fun AppNavigation() {
    AppNavHost(
        rememberNavController()
    )
}

/**
 * App NavHost using Voyager for navigation
 */
@Composable
fun AppNavHost(
    navController: NavHostController
) {
    // Default modifier for screens
    val modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()

    // Access the current context if needed
    val context = LocalContext.current

    // Initialize the navigator
//    val navigator = LocalNavigator.currentOrThrow

    //Create for me NavHost with rememberNavController
    NavHost(
        navController, startDestination = SPLASH_ROUTE
    ) {
        splashScreen(modifier){
            navController.navigate(ONBOARDING_ROUTE) {
                popUpTo(SPLASH_ROUTE) { inclusive = true }
            }
        }
        onboardingScreen(modifier) {
            navController.navigate(ENABLE_PERMISSION_ROUTE) {
                popUpTo(ONBOARDING_ROUTE) { inclusive = true }
            }
        }

        enablePermissionScreen(modifier) {
            val appControllerIntent = android.content.Intent(context, AppController::class.java)
            ContextCompat.startForegroundService(context, appControllerIntent)
        }

        settingScreen(modifier)
    }
}