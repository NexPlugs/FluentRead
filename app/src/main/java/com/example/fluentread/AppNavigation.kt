package com.example.fluentread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.example.fluentread.features.splash.Splash

/**
 * App Navigation this is the entry point for navigation
 */
@Composable
fun AppNavigation() {
    AppNavHost()
}

/**
 * App NavHost
 */
@Composable
fun AppNavHost() {
    val modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()

    Navigator(screen = Splash(modifier = modifier)) {
        CurrentScreen()
    }
}