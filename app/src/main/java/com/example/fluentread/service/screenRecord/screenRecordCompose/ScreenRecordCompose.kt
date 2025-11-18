package com.example.fluentread.service.screenRecord.screenRecordCompose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fluentread.R


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
            screenRecordViewModel.toggleRecord()
        }
    )
}

@Composable
private fun ScreenRecordCompose(
    isRecording: Boolean = false,
    onClickable: () -> Unit = { }
) {

    val icon = if(isRecording) R.drawable.ic_stop_record else R.drawable.ic_record

    Box(
        modifier = Modifier
            .background(Color.Red.copy(alpha = 0.4f), shape = CircleShape)
            .size(54.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(color = Color.Red, shape = CircleShape)
                .width(50.dp)
                .height(50.dp)
                .padding(10.dp)
                .clickable { onClickable() },
            contentAlignment = Alignment.Center
        ) {
            // You can add more UI elements here based on the isRecording state
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}


@Preview
@Composable
private fun ScreenRecordComposePreview() {
    ScreenRecordCompose()
}