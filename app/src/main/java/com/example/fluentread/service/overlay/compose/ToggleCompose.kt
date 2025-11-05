package com.example.fluentread.service.overlay.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fluentread.R
import com.example.fluentread.ui.theme.FluentReadTheme

private val ToggleWidth = 52.dp
private val ButtonSize = 48.dp
private val ButtonPadding = 4.dp
private val ButtonSpacing = 2.dp
private val IconPadding = 10.dp

/**
 * Data class representing an action button in the toggle.
 */
private data class ToggleAction(
    val iconRes: Int,
    val onClick: () -> Unit
)


@Composable
fun Toggle() {
    ToggleCompose(
        actions = listOf(
            ToggleAction(R.drawable.ic_fast_up) { /* TODO: Fast up action */ },
            ToggleAction(R.drawable.ic_up) { /* TODO: Up action */ },
            ToggleAction(R.drawable.ic_down) { /* TODO: Down action */ },
            ToggleAction(R.drawable.ic_fast_down) { /* TODO: Fast down action */ }
        )
    )
}

/**
 * Composable for the vertical toggle with action buttons.
 *
 * @param actions List of actions to display.
 * @param modifier Modifier for the toggle container.
 */
@Composable
private fun ToggleCompose(
    actions: List<ToggleAction>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.small
            )
            .width(ToggleWidth)
            .padding(ButtonPadding)
    ) {
        Column {
            actions.forEachIndexed { index, action ->
                ActionButton(
                    icon = action.iconRes,
                    onTap = action.onClick
                )
                if (index < actions.lastIndex) {
                    Spacer(modifier = Modifier.height(ButtonSpacing))
                }
            }
        }
    }
}

/**
 * Composable for a single action button.
 *
 * @param icon Icon resource ID.
 * @param onTap Click handler.
 * @param modifier Modifier for the button.
 */
@Composable
private fun ActionButton(
    icon: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(ButtonSize)
            .background(
                MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.padding(IconPadding),
        )
    }
}

@Preview
@Composable
fun ToggleComposePreview() {
    FluentReadTheme {
        ToggleCompose(
            actions = listOf(
                ToggleAction(R.drawable.ic_fast_up) { /* TODO: Fast up action */ },
                ToggleAction(R.drawable.ic_up) { /* TODO: Up action */ },
                ToggleAction(R.drawable.ic_down) { /* TODO: Down action */ },
                ToggleAction(R.drawable.ic_fast_down) { /* TODO: Fast down action */ }
            )
        )
    }
}