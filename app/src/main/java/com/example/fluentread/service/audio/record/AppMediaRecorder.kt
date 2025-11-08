package com.example.fluentread.service.audio.record

import com.example.fluentread.service.audio.models.RecordResult
import java.io.File

/**
 * An interface defining methods for audio recording functionality.
 * Implementations of this interface should handle starting audio recording,
 */
interface AppMediaRecorder {
    // Starts audio recording with the specified parameters.
    fun startAudioRecording(
        recordingName: String,
        amplitudePollingInterval: Long = 100L,
    )

    fun stopRecording(): RecordResult

    // Deletes the specified recording file.
    fun deleteRecording(recordingFile: File)

    // Releases resources associated with the recorder.
    fun release()

    // Sets a listener for error events during recording.
    fun setOnErrorListener(listener: OnErrorListener)

    // Sets a listener for when recording starts.
    fun setOnRecordStartedListener(listener: OnRecordStarted)

    // Sets a listener for when recording stops.
    fun setOnRecordStoppedListener(listener: OnRecordStopped)

    // Sets a listener for changes in the current recording duration.
    fun setOnCurrentRecordDurationChangeListener(listener: OnCurrentRecordDurationChange)

    // Sets a listener for changes in the media recorder state.
    fun setOnMediaRecorderStateChangeListener(listener: OnMediaRecorderStateChange)


    /**
     * Callback interface for handling errors during recording.
     */
    fun interface OnErrorListener {
        fun onError(
            appMediaRecorder: AppMediaRecorder,
            what: Int,
            extra: Int
        )
    }

    /**
     * Callback interface for when the recording has started.
     */
    fun interface OnRecordStarted {
        fun onRecordStarted()
    }


    /**
     * Callback interface for when the recording has stopped.
     */
    fun interface OnRecordStopped {
        fun onRecordStopped()
    }

    /**
     * Callback interface for monitoring changes in the current recording duration.
     */
    fun interface OnCurrentRecordDurationChange {
        fun onCurrentRecordDurationChange(
            appMediaRecorder: AppMediaRecorder,
            currentDuration: Long
        )
    }

    /**
     * Callback interface for monitoring changes in the media recorder state.
     */
    fun interface OnMediaRecorderStateChange {
        fun onStateChange(recorderState: MediaRecorderState)
    }
}