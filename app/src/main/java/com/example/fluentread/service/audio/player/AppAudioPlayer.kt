package com.example.fluentread.service.audio.player

import com.example.fluentread.service.audio.models.AudioState
import com.example.fluentread.service.audio.models.ProgressInfo

/**
 * Implementation of AudioPlayer for the FluentRead application.
 */
class AppAudioPlayer: AudioPlayer {
    override val currentState: AudioState
        get() = TODO("Not yet implemented")
    override val currentPlayId: Int
        get() = TODO("Not yet implemented")

    override fun onAudioStateChange(
        hashTrack: Int,
        stateChange: (AudioState) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun onProgressChange(
        hashTrack: Int,
        progressChange: (ProgressInfo) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun onSpeedChange(hashTrack: Int, speedChange: (Float) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun registerTrack(
        srcUrl: String,
        hashTrack: Int,
        startPosition: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun play(srcUrl: String, hashTrack: Int) {
        TODO("Not yet implemented")
    }

    override fun clearTrack(hashTrack: Int) {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun resume() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun seekTo(position: Long) {
        TODO("Not yet implemented")
    }

    override fun currentSpeed(): Float {
        TODO("Not yet implemented")
    }

    override fun currentProgress(): Long {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

    override fun removeAudios(hashList: List<Int>) {
        TODO("Not yet implemented")
    }


}