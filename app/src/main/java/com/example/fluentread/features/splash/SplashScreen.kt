package com.example.fluentread.features.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fluentread.features.components.AnimationType
import com.example.fluentread.features.components.BuildAppLoading
import com.example.fluentread.utils.APP_NAME
import kotlinx.coroutines.delay

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
    LaunchedEffect(true) {
        // Simulate a loading delay
        delay(2000)
        onNextPage()
    }

    SplashScreen(modifier = modifier)
}

/**
 * A simple splash screen composable that centers its content.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 */
@Composable
fun SplashScreen(
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BuildAppLoading(
            imageRes = com.example.fluentread.R.drawable.app_icon,
            sizeDp = 100.dp,
            animationType = AnimationType.SCALE
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = APP_NAME,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = SUP_TITLE,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.W500
            )
        )
    }
}

