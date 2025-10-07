package com.example.fluentread.features.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fluentread.utils.APP_NAME

const val SUP_TITLE = "Hands-free scrolling"


/**
 * The entry point for the Splash Screen feature.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 * @param onNextPage Lambda to be invoked when navigating to the next page.
 */
@Composable
fun SplashRoute(
    modifier: Modifier = Modifier,
    onNextPage: () -> Unit = { }
) {
    SplashScreen(modifier = modifier)
}

/**
 * A simple splash screen composable that centers its content.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 */
@Composable
fun SplashScreen(
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = APP_NAME,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = SUP_TITLE,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.W500
            )
        )
    }
}