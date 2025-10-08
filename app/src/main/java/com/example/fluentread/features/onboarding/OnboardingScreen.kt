package com.example.fluentread.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fluentread.R
import com.example.fluentread.features.components.BuildButton
import com.example.fluentread.utils.INTRODUCTION_DESC
import com.example.fluentread.utils.INTRODUCTION_TITLE

/**
 * Entry route for the Onboarding feature.
 *
 * @param modifier The [Modifier] to be applied to this route.
 * @param onNextPage The callback to be invoked when the user has completed onboarding.
 */
@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
    onNextPage: () -> Unit = {}
) {
    OnboardingScreen(modifier = modifier, onNextPage = onNextPage)
}


/**
 * A simple onboarding screen composable that centers its content.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 * @param onNextPage The callback to be invoked when the user presses the "Get Started" button.
 */
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onNextPage: () -> Unit = {}
) {


    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.img_introduction),
            contentDescription = null,
            //Modifier with size 200.dp and radius 20f
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(200.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = INTRODUCTION_TITLE,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = INTRODUCTION_DESC,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        BuildButton(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.primary,
            onPress =  onNextPage,
            enableWidth = false,
            height = 52.dp,
            content = {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            },
            radius = 12f
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}