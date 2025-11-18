package com.example.fluentread.service.media.audio.models

import android.media.MediaRecorder

/**
 * Data class representing the result of an audio recording operation.
 * @param success Indicates whether the recording was successful.
 * @param filePath The file path of the recorded audio, if successful.
 * @param errorMessage An error message, if the recording failed.
 * @param mimeType The MIME type of the recorded audio file.
 * @param extraData Additional metadata related to the recording.
 * @param fileName The name of the recorded audio file.
 * @param duration The duration of the recorded audio in milliseconds.
 *
 */
data class RecordResult(
    val success: Boolean,
    val filePath: String? = null,
    val errorMessage: String? = null,
    val mimeType: String? = null,
    val extraData : Map<String, Any>? = null,
    val fileName: String? = null,
    val duration: Long? = null
)


fun Int.getMimType(): String {
    return when (this) {
        MediaRecorder.AudioEncoder.AAC -> "audio/mpeg" // MP3
        MediaRecorder.AudioEncoder.AMR_NB -> "audio/3gpp" // AMR
        MediaRecorder.AudioEncoder.HE_AAC -> "audio/mp4a-latm" // AAC
        MediaRecorder.AudioEncoder.VORBIS -> "audio/vorbis" // OGG
        else -> "application/octet-stream" // Default binary type
    }
}