package com.example.fluentread.service.audio.models

/**
 * Holds information about the current progress of audio playback.
 * @param currentPosition The current position in the audio track (in milliseconds).
 * @param duration The total duration of the audio track (in milliseconds).
 */
data class ProgressInfo(
    val currentPosition: Long,
    val duration: Long,
)