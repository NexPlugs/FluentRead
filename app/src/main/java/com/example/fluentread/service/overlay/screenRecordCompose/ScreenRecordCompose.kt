package com.example.fluentread.service.overlay.screenRecordCompose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun ScreenRecordCompose(
    screenRecordViewModel: ScreenRecordViewModel,
    onStartRecording: () -> Unit = { },
    onStopRecording: () -> Unit = { }
) {
    val uiState = screenRecordViewModel.state.collectAsState().value
    ScreenRecordCompose(
        isRecording = uiState.isRecording,
        onClickable = {
            if (uiState.isRecording) {
                onStopRecording()
            } else {
                onStartRecording()
            }
        }
    )
}

@Composable
private fun ScreenRecordCompose(
    isRecording: Boolean = false,
    onClickable: () -> Unit = { }
) {

    Box(
        modifier = Modifier
            .background(color = Color.Red, shape = CircleShape)
            .width(50.dp)
            .height(50.dp)
            .border(
                width = 2.dp,
                shape = CircleShape,
                color = Color.Red.copy(alpha = 0.2f),
            )
            .clickable { onClickable() },
        contentAlignment = Alignment.Center
    ) {
        // You can add more UI elements here based on the isRecording state
        if(!isRecording)
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Recording Icon",
                tint = Color.White
            )
    }
}


@Preview
@Composable
private fun ScreenRecordComposePreview() {
    ScreenRecordCompose()
}