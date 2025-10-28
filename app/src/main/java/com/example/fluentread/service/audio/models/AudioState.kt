package com.example.fluentread.service.audio.models

/**
 * Represents the various states of audio playback.
 */
enum class AudioState {
    IDLE,
    PREPARING,
    PLAYING,
    PAUSED,
    STOPPED,
    COMPLETED,
}