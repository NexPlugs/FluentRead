package com.example.fluentread.service.media.audio.models

import android.media.MediaRecorder
import android.os.Parcel
import android.os.Parcelable

const val SAMPLING_RATE_16KHZ = 16000

const val ENCODING_BIT_RATE_32KBPS = 32000

const val MONO_CHANNEL = 1


/**
 * Configuration settings for audio recording.
 */
@Suppress("DEPRECATION")
data class AudioConfig(
    /**
     * * Audio source for recording. Default is microphone.
     * MIC: use the device's microphone.
     * VOICE_COMMUNICATION: optimized for voice communication. use for video voice chat
     * VOICE_RECOGNITION: optimized for voice recognition. use for speech-to-text applications.
     * CAMCORDER: optimized for video recording. use for recording video with audio.
     * .....
     */
    val audioSource: Int = MediaRecorder.AudioSource.MIC,

    /**
     * * Output format for the recorded audio. Default is AAC_ADTS.
     * AAC_ADTS: Advanced Audio Coding in ADTS container. -> minimal file size, good quality
     * MPEG_4: MPEG-4 Part 14 container format. -> widely supported, good quality
     * THREE_GPP: 3GPP container format. -> good for streaming, lower quality
     */
    val outputFormat: Int = MediaRecorder.OutputFormat.AAC_ADTS,

    /**
     * * Audio encoder for the recorded audio. Default is AAC.
     * AAC: Advanced Audio Coding. -> good quality, efficient compression use for voice music
     * AMR_NB: Adaptive Multi-Rate Narrowband. -> lower quality, smaller file size -> use for call
     * AMR_WB: Adaptive Multi-Rate Wideband. -> better quality than AMR_NB, larger file size
     */
    val audiEncoder: Int = MediaRecorder.AudioEncoder.AAC,

    /**
     * Sampling rate for the recorded audio. Default is 16kHz.
     * 8000 (small and use for call) ,
     * 11025, 16000 (good voice, safe buffer, good quality, use voice recorder , Chat voice, ..),
     * 22050, 44100 (use for CD with high quality) Hz.
     * Higher sampling rates generally result in better audio quality but larger file sizes.
     */
    val audiSamplingRate: Int = SAMPLING_RATE_16KHZ,

    /**
     * Encoding bit per seconds to safe audio. Default is 32kbps.
     * Common bit rates for audio recording range from 16kbps to 320kbps.
     * Higher bit rates generally result in better audio quality but larger file sizes.
     */
    val encodeBitRate: Int = ENCODING_BIT_RATE_32KBPS,

    /**
     * Number of audio channels. Default is mono channel.
     * MONO_CHANNEL: single channel audio. -> smaller file size, use for voice recording
     * STEREO_CHANNEL: two channel audio. -> better quality, use for music recording
     */
    val audiChannel: Int = MONO_CHANNEL,

    // Popup features option
    val showAmplitudePopUp: Boolean = false,
    val autoRecord: Boolean = true,
): Parcelable {
    constructor(parcel: Parcel): this(
        audioSource = parcel.readInt(),
        outputFormat = parcel.readInt(),
        audiEncoder = parcel.readInt(),
        audiSamplingRate = parcel.readInt(),
        encodeBitRate = parcel.readInt(),
        audiChannel = parcel.readInt(),
        showAmplitudePopUp = parcel.readByte() != 0.toByte(),
        autoRecord = parcel.readByte() != 0.toByte()
    )


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(audioSource)
        dest.writeInt(outputFormat)
        dest.writeInt(audiEncoder)
        dest.writeInt(audiSamplingRate)
        dest.writeInt(encodeBitRate)
        dest.writeInt(audiChannel)
        dest.writeByte(if (showAmplitudePopUp) 1 else 0)
        dest.writeByte(if (autoRecord) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<AudioConfig> {
        override fun createFromParcel(parcel: Parcel): AudioConfig {
            return AudioConfig(parcel)
        }

        override fun newArray(size: Int): Array<AudioConfig?> {
            return arrayOfNulls(size)
        }
    }

}