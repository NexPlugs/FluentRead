package com.example.fluentread.service.audio.player

import com.example.fluentread.service.audio.models.AudioState
import com.example.fluentread.service.audio.models.ProgressInfo

/**
 * Interface defining the contract for an audio player.
 */
@Suppress("TooManyFunctions")
interface AudioPlayer {
    /**
     * Gets the current state of the audio player.
     */
    val currentState: AudioState
    
    /**
     * Gets the identifier of the currently playing audio track.
     */
    val currentPlayId: Int
    
    /**
     * Registers a listener for state changes of the audio track.
     * @param hashTrack The identifier of the audio track.
     * @param stateChange A lambda function that receives the new audio state.
     */
    fun onAudioStateChange(hashTrack: Int, stateChange: (AudioState) -> Unit)

    /**
     * Registers a listener for progress changes of the audio track.
     * @param hashTrack The identifier of the audio track.
     * @param progressChange A lambda function that receives progress information.
     */
    fun onProgressChange(hashTrack: Int, progressChange: (ProgressInfo) -> Unit)

    /**
     * Registers a listener for speed changes of the audio track.
     * @param hashTrack The identifier of the audio track.
     * @param speedChange A lambda function that receives the new playback speed.
     */
    fun onSpeedChange(hashTrack: Int, speedChange: (Float) -> Unit)

    /**
     * Registers a track with the audio player.
     * @param srcUrl The source URL of the audio track.
     * @param hashTrack The identifier of the audio track.
     * @param startPosition The starting position for playback (in milliseconds).
     */
    fun registerTrack(srcUrl: String, hashTrack: Int, startPosition: Int)

    /**
     * Plays the specified audio track.
     * @param srcUrl The source URL of the audio track.
     * @param hashTrack The identifier of the audio track.
     */
    fun play(srcUrl: String, hashTrack: Int)

    /**
     * Clears the specified audio track from the player.
     */
    fun clearTrack(hashTrack: Int)

    /**
     * Pauses audio playback.
     */
    fun pause()

    /**
     * Resumes audio playback.
     */
    fun resume()

    /**
     * Stops audio playback.
     */
    fun stop()

    /**
     * Sets the playback speed.
     * @param speed The desired playback speed.
     */
    fun seekTo(position: Long)

    /**
     * Get the current playback speed.
     */
    fun currentSpeed(): Float

    /**
     * Gets the current playback progress.
     */
    fun currentProgress(): Long

    /**
     * Disposes of the audio player and releases resources.
     */
    fun dispose()

    /**
     * Removes audio tracks corresponding to the provided list of identifiers.
     * @param hashList A list of audio track identifiers to be removed.
     */
    fun removeAudios(hashList: List<Int>)

}

