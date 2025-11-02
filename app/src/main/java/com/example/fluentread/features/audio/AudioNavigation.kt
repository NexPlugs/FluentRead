package com.example.fluentread.features.audio

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val AUDIO_NAVIGATION_ROUTE = "audio_navigation_route"


fun NavGraphBuilder.audioScreen(
    modifier: Modifier
) {
    // Define audio-related navigation routes here
    composable(
        AUDIO_NAVIGATION_ROUTE
    ) {
        AudioPlayScreen(modifier = modifier)
    }
}