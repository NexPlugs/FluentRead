package com.example.fluentread.service.screenRecord

import android.content.Intent
import com.example.fluentread.service.screenRecord.models.ScreenRecordResult

/**
 * Interface defining the contract for an application screen recorder.
 */
interface AppScreenRecorder {

    /**
     * Starts a new screen recording with the specified name.
     * @param recordingName The name to assign to the recording.
     * @param intent The intent containing screen capture permissions.
     */
    fun startRecording(recordingName: String, intent: Intent)

    /** Stops the ongoing screen recording and returns the result. */
    fun stopRecording(): ScreenRecordResult

    /** Pauses the ongoing screen recording. */
    fun pause()

    /** Resumes the paused screen recording. */
    fun resume()

    /** Releases resources associated with the screen recorder. */
    fun isRecording(): Boolean

    /**
     * Callback interface for handling errors during screen recording.
     */
    fun interface OnErrorListener {
        fun onError(what: Int, extra: Int)
    }

    fun interface OnMediaProjectionStopListener {
        fun onStop()
    }

    fun interface OnCapturedContentListener {
        fun onCapturedContent(width: Int, height: Int)
    }

    fun interface OnCaptureContentVisibilityListener {
        fun onContentVisibilityChanged(isVisible: Boolean)
    }

    /**
     * Sets the error listener for the screen recorder.
     * @param listener The listener to be invoked on errors.
     */
    fun setOnErrorListener(listener: OnErrorListener)

    /** Sets the media projection stop listener for the screen recorder.
     * @param listener The listener to be invoked when media projection stops.
     */
    fun setOnMediaProjectionStopListener(listener: OnMediaProjectionStopListener)

    /** Sets the captured content listener for the screen recorder.
     * @param listener The listener to be invoked when content is captured.
     */
    fun setOnCapturedContentListener(listener: OnCapturedContentListener)

    /** Sets the capture content visibility listener for the screen recorder.
     * @param listener The listener to be invoked when content visibility changes.
     */
    fun setOnCaptureContentVisibilityListener(listener: OnCaptureContentVisibilityListener)

}