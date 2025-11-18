package com.example.fluentread.service.audio.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AudioRecordCompose(
    recordType: RecordType = RecordType.CIRCLE,
    audioRecordViewModel: AudioViewModel,

    ) {
    val uiState = audioRecordViewModel.uiState.collectAsState().value

    val modifier = Modifier
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .fillMaxWidth()
        .background(Color.White, RoundedCornerShape(12.dp))
        .padding(16.dp)

    when (recordType) {
        RecordType.CIRCLE -> {
            AudioRecordCircleCompose(
                modifier = modifier,
                isRecording = uiState.isRecording,
                amplitude = uiState.amplitudeTracking.toInt()
            )
        }

        RecordType.WAVE -> {
            AudioRecordWaveCompose(
                modifier = modifier,
                isRecording = uiState.isRecording
            )
        }
    }
}

// ───────────────────────────────────────────────────────────────
// CIRCLE VISUALIZER
// ───────────────────────────────────────────────────────────────

@Composable
fun AudioRecordCircleCompose(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    amplitude: Int = 50,
) {
    // Convert amplitude (0–100) → scale (1f–1.5f)
    val targetPulseScale = remember(amplitude, isRecording) {
        if (isRecording) 1f + (amplitude.coerceIn(0, 100) / 100f * 0.5f)
        else 1f
    }

    // Smooth pulse animation following amplitude
    val pulseScale by animateFloatAsState(
        targetValue = targetPulseScale,
        animationSpec = tween(
            durationMillis = 140,
            easing = LinearOutSlowInEasing
        ),
        label = "AmplitudePulseScale"
    )

    // Glow alpha (normal)
    val glowAlpha by animateFloatAsState(
        targetValue = if (isRecording) 0.35f else 0f,
        animationSpec = tween(300, easing = LinearOutSlowInEasing),
        label = "CircleGlowAlpha"
    )

    // Inner shrink/expand
    val innerScale by animateFloatAsState(
        targetValue = if (isRecording) 1f else 0.85f,
        animationSpec = tween(300, easing = LinearOutSlowInEasing),
        label = "InnerCircleScale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer pulse following amplitude
        Box(
            modifier = Modifier
                .size(80.dp * targetPulseScale)
                .graphicsLayer { alpha = glowAlpha }
                .background(
                    color = Color.Red.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        )

        // Inner circle
        Box(
            modifier = Modifier
                .size(60.dp * innerScale)
                .background(Color.Red, CircleShape)
        )
    }
}

// ───────────────────────────────────────────────────────────────
// WAVE VISUALIZER
// ───────────────────────────────────────────────────────────────

@Composable
fun AudioRecordWaveCompose(
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    waveColor: Color = Color.Blue,
    numberOfBars: Int = 5,
    listAmplitude: List<Int> = listOf(10, 30, 50, 70, 90),
) {
    // Normalize amplitude list to match bar count
    val barAmplitudes = remember(listAmplitude, numberOfBars) {
        listAmplitude.take(numberOfBars).map { it.coerceAtLeast(0) }
    }

    // Global animation controlling all bars
    val progress by animateFloatAsState(
        targetValue = if (isRecording) 1f else 0f,
        animationSpec = tween(
            durationMillis = 900,
            easing = LinearOutSlowInEasing
        ),
        label = "WaveProgress"
    )

    Box(
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            barAmplitudes.forEach { amp ->
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height((amp * progress).dp)
                        .background(waveColor, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

// ───────────────────────────────────────────────────────────────
// PREVIEW
// ───────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun AudioRecordComposePreview() { }
