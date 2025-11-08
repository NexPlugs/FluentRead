package com.example.fluentread.service.screenRecord.models

import android.media.MediaRecorder


/**
 * Data class representing the configuration for screen recording.
 */
data class ScreenRecordConfig(
    /** videoSource: The source of the video to be recorded. Default is SURFACE. */
    val videoSource: Int = MediaRecorder.VideoSource.SURFACE,
    /** audioSource: The source of the audio to be recorded. Default is MIC. */
    val audioSource: Int = MediaRecorder.AudioSource.MIC,
    /** outputFormat: The format of the output file. Default is MPEG_4. */
    val outputFormat: Int = MediaRecorder.OutputFormat.MPEG_4,
    /** videoBitRate: The bit rate for video encoding. Default is 8 Mbps. */
    val videoEncodingBitRate: Int = 8 * 1000 * 1000,
    /** videoSizeWidth: The width of the video to be recorded. Default is 1920 pixels. */
    val videoEncoder: Int = MediaRecorder.VideoEncoder.H264,
    /** videoSizeHeight: The height of the video to be recorded. Default is 1080 pixels. */
    val audioEncoder: Int = MediaRecorder.AudioEncoder.AAC,
    /** audioEncodingBitRate: The bit rate for audio encoding. Default is 320 kbps. */
    val audioEncodingBitRate: Int = 320 * 1000,
    /** audioSamplingRate: The sampling rate for audio recording. Default is 48 kHz. */
    val audioSamplingRate: Int = 48000,
    /** videoFrameRate: The frame rate for video recording. Default is 30 fps. */
    val videoFrameRate: Int = 30,
    /** isAudioEnable: Flag indicating whether audio recording is enabled. Default is true. */
    val isAudioEnable: Boolean = true,
)