package com.example.fluentread.service.screenRecord.models


/**
 * Data class representing the configuration for screen recording.
 */
data class ScreenRecordConfig(
    val videoWidth: Int,
    val videoHeight: Int,
    val videoBitrate: Int,
    val videoFps: Int,
    val audioSampleRate: Int,
    val audioBitrate: Int,
)