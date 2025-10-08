package com.example.fluentread.features.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * AppLoading is a composable function that displays a loading animation.
 * It supports two types of animations: rotation and scaling.
 */
enum class AnimationType {
    ROTATE, SCALE;
    val value: String
        get() = when (this) {
            ROTATE -> "rotate_animation"
            SCALE -> "scale_animation"
        }
}

/**
 * AppLoading is a composable function that displays a loading animation.
 * It supports two types of animations: rotation and scaling.
 * @param modifier The modifier to be applied to the loading image.
 * @param animationType The type of animation to be applied to the loading image.
 * @param durationMillis The duration of the animation in milliseconds.
 * @param sizeDp The size of the loading image in dp.
 * @param imageRes The resource ID of the image to be displayed as the loading animation.
 */
@Composable
fun BuildAppLoading(
    modifier: Modifier = Modifier,
    animationType: AnimationType = AnimationType.ROTATE,
    durationMillis: Int = 1200,
    sizeDp: Dp = 64.dp, // default size
    imageRes: Int, // your local image resource
) {

    // Create an infinite transition for the loading animation
    val infiniteTransition = rememberInfiniteTransition(label = "loading_animation")

    val animationModifier = when (animationType) {
        AnimationType.ROTATE -> {
            val angle = infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = durationMillis, easing = LinearEasing)
                ),
                label = animationType.value
            )
            Modifier.graphicsLayer {
                this.rotationZ = angle.value
            }
        }
        AnimationType.SCALE -> {
            val scale = infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = durationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = animationType.value
            )
            Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
        }
    }
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null,
        modifier = modifier.then(animationModifier).size(sizeDp)
    )
}
