package com.example.fluentread.service.media.audio.player

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
    fun onProgressChange(hashTrack: Int, progressChange: ProgressInfo)

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
     * Prepares the audio track for playback.
     */
    fun prepare(srcUrl: String, hashTrack: Int)

    /**
     * Pauses audio playback.
     */
    fun pause()

    /**
     * Resumes audio playback.
     */
    fun resume(hasTrack: Int)

    /**
     * Stops audio playback.
     */
    fun stop()

    /**
     * Seeks to a specific position in the audio track.
     * @param position The position to seek to (in milliseconds).
     * @param hashTrack The identifier of the audio track.
     */
    fun seekTo(position: Long, hashTrack: Int)

    /**
     * Get the current playback speed.
     */
    fun currentSpeed(): Float


    /**
     * Changes the playback speed.
     * @param isIncrease If true, increases the speed; if false, decreases the speed.
     */
    fun changeSpeed(isIncrease: Boolean)

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
     * @param audioHash The identifier of the audio track to be removed.
     */
    fun removeAudio(audioHash: Int)

    /**
     * Removes audio tracks corresponding to the provided list of identifiers.
     * @param audioHashList The list of identifiers of the audio tracks to be removed.
     */
    fun removeListAudio(audioHashList: List<Int>)

}

















