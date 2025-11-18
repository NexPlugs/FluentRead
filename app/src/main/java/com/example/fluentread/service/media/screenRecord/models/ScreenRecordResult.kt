package com.example.fluentread.service.media.screenRecord.models

/**
 * Data class representing the result of a screen recording session.
 *
 * @property filePath The file path where the recorded video is saved.
 * @property durationMillis The duration of the recording in milliseconds.
 * @property fileSizeBytes The size of the recorded file in bytes.
 */
data class ScreenRecordResult(
    val filePath: String,
    val durationMillis: Long,
    val fileSizeBytes: Long
)