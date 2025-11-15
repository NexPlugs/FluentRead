package com.example.fluentread.service.screenRecord.models

import android.media.MediaRecorder
import android.os.Parcel
import android.os.Parcelable

/**
 * Data class representing the configuration for screen recording.
 */
@Suppress("DEPRECATION")
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
): Parcelable {
    constructor(parcel: Parcel) : this(
        videoSource = parcel.readInt(),
        audioSource = parcel.readInt(),
        outputFormat = parcel.readInt(),
        videoEncodingBitRate = parcel.readInt(),
        videoEncoder = parcel.readInt(),
        audioEncoder = parcel.readInt(),
        audioEncodingBitRate = parcel.readInt(),
        audioSamplingRate = parcel.readInt(),
        videoFrameRate = parcel.readInt(),
        isAudioEnable = parcel.readByte() != 0.toByte(),
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(videoSource)
        parcel.writeInt(audioSource)
        parcel.writeInt(outputFormat)
        parcel.writeInt(videoEncodingBitRate)
        parcel.writeInt(videoEncoder)
        parcel.writeInt(audioEncoder)
        parcel.writeInt(audioEncodingBitRate)
        parcel.writeInt(audioSamplingRate)
        parcel.writeInt(videoFrameRate)
        parcel.writeByte(if (isAudioEnable) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ScreenRecordConfig> {
        override fun createFromParcel(parcel: Parcel): ScreenRecordConfig {
            return ScreenRecordConfig(parcel)
        }

        override fun newArray(size: Int): Array<ScreenRecordConfig?> {
            return arrayOfNulls(size)
        }
    }
}

