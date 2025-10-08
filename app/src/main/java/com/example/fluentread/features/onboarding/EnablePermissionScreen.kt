package com.example.fluentread.features.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


/**
 * Entry route for the Enable Permission feature.
 *
 * @param modifier The [Modifier] to be applied to this route.
 * @param onNextPage The callback to be invoked when the user has completed enabling permissions.
 */
@Composable
fun EnablePermissionRoute(
    modifier: Modifier = Modifier,
    onNextPage: () -> Unit = {}
) {
    EnablePermissionScreen(modifier = modifier)
}

/**
 * A simple enable permission screen composable.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 */
@Composable
fun EnablePermissionScreen(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(text = "Enable Permission Screen")
    }
}


