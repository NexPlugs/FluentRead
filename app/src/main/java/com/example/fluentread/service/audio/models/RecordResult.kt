package com.example.fluentread.service.audio.models

/**
 * Data class representing the result of an audio recording operation.
 * @param success Indicates whether the recording was successful.
 * @param filePath The file path of the recorded audio, if successful.
 * @param errorMessage An error message, if the recording failed.
 * @param mimeType The MIME type of the recorded audio file.
 * @param extraData Additional metadata related to the recording.
 * @param fileName The name of the recorded audio file.
 *
 */
data class RecordResult(
    val success: Boolean,
    val filePath: String? = null,
    val errorMessage: String? = null,
    val mimeType: String? = null,
    val extraData : Map<String, Any>? = null,
    val fileName: String? = null
)

