package com.example.fluentread.service.audio

/**
 * Represents the various states of a media recorder.
 */
enum class MediaRecorderState {
    // The recorder is idle and not recording.
    IDLE,
    // The recorder is currently recording audio.
    RECORDING,
    // The recorder is paused.
    PAUSED,
    // The recorder has been stopped.
    STOPPED,
    // An error has occurred in the recorder.
    ERROR,
    // The recorder is prepared and ready to start recording.
    PREPARED,
}