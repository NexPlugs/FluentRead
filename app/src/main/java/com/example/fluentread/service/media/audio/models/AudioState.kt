package com.example.fluentread.service.media.audio.models

/**
 * Represents the various states of audio playback.
 */
enum class AudioState {
    IDLE,
    PREPARED,
    PLAYING,
    PAUSED,
    STOPPED,
    COMPLETED,
    UNSET
}