package com.example.fluentread.service.audio.player

import android.util.Log
import com.example.fluentread.service.audio.models.AudioState
import com.example.fluentread.service.audio.models.ProgressInfo
import kotlinx.coroutines.CoroutineScope

/**
 * Data class representing information about an audio track.
 */
data class TrackInfo(
    val srcUrl: String,
    val hashTrack: Int,
    val startPosition: Int = 0,
)

/**
 * Implementation of AudioPlayer for the FluentRead application.
 */
class AppAudioPlayer(
    val audiSpeed: Float? = null,
    val audioScope: CoroutineScope,
    private val coreMediaPlayer: CoreMediaPlayer,
): AudioPlayer {
    companion object {
        private const val TAG = "AppAudioPlayer"

        /**
         * Speed values for audio playback.
         */
        private const val DEFAULT_SPEED = 1.0f
        private const val MAX_SPEED = 3.0f
        private const val SPEED_INCREMENT = 0.25f
    }

    /**
     * Map of state change listeners for audio tracks.
     */
    private val stateListeners: MutableMap<Int, (AudioState) -> Unit> = mutableMapOf()

    /**
     * Map of progress change listeners for audio tracks.
     */
    private val progressListeners: MutableMap<Int, (ProgressInfo) -> Unit> = mutableMapOf()

    /**
     * Map of speed change listeners for audio tracks.
     */
    private val speedListeners: MutableMap<Int, (Float) -> Unit> = mutableMapOf()

    /**
     * Map of registered audio tracks.
     */
    private val registeredTracks: MutableMap<Int, TrackInfo> = mutableMapOf()

    // Internal state variables
    private var playerState: AudioState = AudioState.IDLE

    // Currently playing audio track identifier
    private var currentAudioHash: Int = -1

    override val currentState: AudioState get() = playerState

    override val currentPlayId: Int get() = currentAudioHash

    override fun onAudioStateChange(
        hashTrack: Int,
        stateChange: (AudioState) -> Unit
    ) {
        Log.v(TAG, "Registering state change listener for track: $hashTrack")
        // Register the state change listener
        stateListeners[hashTrack] = stateChange
    }

    override fun onProgressChange(
        hashTrack: Int,
        progressChange: (ProgressInfo) -> Unit
    ) {
        Log.v(TAG, "Registering progress change listener for track: $hashTrack")
        // Register the progress change listener
        progressListeners[hashTrack] = progressChange
    }

    override fun onSpeedChange(hashTrack: Int, speedChange: (Float) -> Unit) {
        Log.v(TAG, "Registering speed change listener for track: $hashTrack")
        // Register the speed change listener
        speedListeners[hashTrack] = speedChange
    }


    override fun registerTrack(
        srcUrl: String,
        hashTrack: Int,
        startPosition: Int
    ) {
        Log.d(TAG, "Registering track: $hashTrack with source: $srcUrl at position: $startPosition")
        // Store the track information
        if(registeredTracks.containsKey(hashTrack)) {
            Log.w(TAG, "Track $hashTrack is already registered. Overwriting existing track.")
            return
        }
        registeredTracks[hashTrack] = TrackInfo(
            srcUrl = srcUrl,
            hashTrack = hashTrack,
            startPosition = startPosition
        )
    }

    override fun clearTrack(hashTrack: Int) {
        Log.d(TAG, "Clearing track: $hashTrack")
        // Remove the track information and listeners
        registeredTracks.remove(hashTrack)
        stateListeners.remove(hashTrack)
        progressListeners.remove(hashTrack)
        speedListeners.remove(hashTrack)
    }


    override fun play(srcUrl: String, hashTrack: Int) {
        if(hashTrack != currentAudioHash) {
            Log.d(TAG, "Playing track: $hashTrack from source: $srcUrl")
            currentAudioHash = hashTrack
            setAudio(srcUrl, hashTrack, true)
            return
        }
        when(playerState) {
            AudioState.PAUSED -> {
                Log.d(TAG, "Resuming playback for track: $hashTrack")
                coreMediaPlayer.start()
                playerState = AudioState.PLAYING
                notifyStateChange(hashTrack, playerState)
            }
            AudioState.PLAYING -> {
                Log.d(TAG, "Track: $hashTrack is already playing.")
            }
            else -> {
                Log.d(TAG, "Starting playback for track: $hashTrack")
                setAudio(srcUrl, hashTrack, true)
            }
        }
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

    /**
     * Resets the media player to its initial state.
     */
    private fun resetMediaPlayer() {
        Log.d(TAG, "Resetting media player.")
        coreMediaPlayer.reset()
        playerState = AudioState.IDLE
        if(currentAudioHash != - 1) {
            currentAudioHash = -1
        }
    }

    /**
     * Sets the audio source for playback.
     * @param srcUrl The source URL of the audio track.
     * @param hash The identifier of the audio track.
     * @param autoPlay Whether to start playback automatically after setting the source.
     */
    private  fun setAudio(srcUrl: String, hash: Int, autoPlay: Boolean = true) {
        Log.d(TAG, "Setting audio source: $srcUrl for track: $hash with autoPlay: $autoPlay")

        currentAudioHash = hash

        try {
            coreMediaPlayer.run {
                setOnCompletionListener {}
                setOnErrorListener { _, _ ->
                    Log.e(TAG, "Media player error for track: $hash")
                    true
                }
                playerState = AudioState.PREPARING
                notifyStateChange(hash, playerState)

                setSource(srcUrl)
                prepare()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting audio source: $srcUrl for track: $hash", e)
            playerState = AudioState.IDLE
        }
    }

    /**
     * Notifies registered listeners about a state change for a specific track.
     */
    private fun notifyStateChange(hashTrack: Int, newState: AudioState) {
        stateListeners[hashTrack]?.invoke(newState)
    }
}