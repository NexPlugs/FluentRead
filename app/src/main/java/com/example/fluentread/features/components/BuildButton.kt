package com.example.fluentread.features.components
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * BuildButton is a customizable button component that supports loading state, hover effects, and custom dimensions.
 * It allows you to specify the button's color, radius, width, height, and border color.
 * It also provides a loading indicator when the button is in a loading state.
 * * @param color The color of the button.
 * * @param radius The radius of the button.
 * * @param width The width of the button. If null, the button will fill the available width.
 * * @param height The height of the button. If null, the button will have a default height.
 * * @param enableWidth A flag to enable the width of the button. If false, the button will fill the available width.
 * * @param loading A flag to enable the loading state of the button. If true, a loading indicator will be shown.
 * * @param borderColor The border color of the button. If null, the button will have no border.
 * * @param onPress The action to be performed when the button is pressed.
 * * @param content The content of the button. This is a composable function that will be displayed inside the button.
 */

@Composable
fun BuildButton(
    modifier: Modifier = Modifier,
    /// The color of the button.
    color: Color = MaterialTheme.colorScheme.primary,
    /// The radius of the button.
    radius: Float = 10f,
    /// The width of the button.
    width: Dp? = null,
    /// The height of the button.
    height: Dp? = null,
    /// A flag to enable the width of the button.
    enableWidth: Boolean = true,
    /// A flag to enable the loading state of the button.
    loading: Boolean = false,
    /// The border color of the button.
    borderColor: Color? = null,
    /// The action to be performed when the button is pressed.
    onPress: () -> Unit,
    /// The content of the button.
    content: @Composable () -> Unit
) {
    /// The state of the button when it is hovered.
    val isHovered by remember { mutableStateOf(false) }
    /// The background color of the button.
    /// animateColorAsState is used to animate the color change when the button is hovered.
    val backgroundColor by animateColorAsState(
        if (isHovered) color.copy(alpha = 0.75f) else color, label = ""
    )
    val borderColorState = borderColor ?: color

    val btnModifier = modifier
        .then(
            if (enableWidth && width != null) Modifier.width(width) else Modifier.fillMaxWidth()
        )

    Box(
        modifier = btnModifier
            .then(
                if (height != null) Modifier.height(height) else Modifier
            )
            .clip(RoundedCornerShape(radius.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isHovered) Color.Transparent else borderColorState,
                shape = RoundedCornerShape(radius.dp)
            )
            .shadow(if (isHovered) 6.dp else 0.dp)
    ) {
        Button(
            onClick = { if (!loading) onPress() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(radius.dp),
            enabled = !loading
        ) {
            if (loading) {
                Row(
                    modifier = btnModifier,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size((height ?: 50.dp) * 0.6f)
                            .background(backgroundColor),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                content()
            }
        }
    }
}