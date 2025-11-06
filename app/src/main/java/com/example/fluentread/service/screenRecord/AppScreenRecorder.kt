package com.example.fluentread.service.screenRecord

/**
 * Interface defining the contract for an application screen recorder.
 */
interface AppScreenRecorder {

    fun startRecording()

    fun stopRecording()

    fun pause()

    fun resume()

    fun isRecording(): Boolean

    fun interface OnErrorListener {
        fun onError(errorCode: Int, errorMessage: String)
    }
}