package com.example.fluentread.service.media.audio.player

import android.util.Log
import com.example.fluentread.service.audio.models.AudioState
import com.example.fluentread.service.audio.models.ProgressInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Data class representing information about an audio track. */
data class TrackInfo(
    val srcUrl: String,
    val hashTrack: Int,
    val startPosition: Int = 0,
)

/** Implementation of AudioPlayer for the FluentRead application. */
class AppAudioPlayer(
    val speed: Float? = null,
    val maxSpeed: Float? = null,
    val speedChange: Float? = null,

    val audioSpeed: Float? = null,
    val autoPlay: Boolean = true,
    val audioScope: CoroutineScope,
    private val coreMediaPlayer: CoreMediaPlayer,
) : AudioPlayer {
    companion object {
        private const val TAG = "AppAudioPlayer"
        private const val DEFAULT_SPEED = 1.0f
        private const val MAX_SPEED = 3.0f
        private const val SPEED_CHANGE = 0.25f
    }

    private val stateListeners = mutableMapOf<Int, (AudioState) -> Unit>()
    private val progressListeners = mutableMapOf<Int, (ProgressInfo) -> Unit>()
    private val speedListeners = mutableMapOf<Int, (Float) -> Unit>()
    private val seek = mutableMapOf<Int, Int>()
    private val registeredTracks = mutableMapOf<Int, TrackInfo>()

    private var currentSpeed: Float = DEFAULT_SPEED
        get() = speed ?: audioSpeed ?: DEFAULT_SPEED
        set(value)  {
            if(value != field) {
                field = value
                onSpeedChange(currentAudioHash) { value }
            }
        }

    private val maximumSpeed: Float
        get() = maxSpeed ?: MAX_SPEED

    private val speedStep: Float
        get() = speedChange ?: SPEED_CHANGE

    private var playerState: AudioState = AudioState.UNSET
    private var currentAudioHash: Int = -1


    override val currentState: AudioState get() = playerState
    override val currentPlayId: Int get() = currentAudioHash

    override fun onAudioStateChange(hashTrack: Int, stateChange: (AudioState) -> Unit) {
        Log.v(TAG, "Registering state change listener for track: $hashTrack")
        stateListeners[hashTrack] = stateChange
    }

    override fun onProgressChange(hashTrack: Int, progressChange: ProgressInfo) {
        Log.v(TAG, "Registering progress change listener for track: $hashTrack")
        progressListeners[hashTrack] = { progressChange }
    }

    override fun onSpeedChange(hashTrack: Int, speedChange: (Float) -> Unit) {
        Log.v(TAG, "Registering speed change listener for track: $hashTrack")
        speedListeners[hashTrack] = speedChange
    }

    override fun registerTrack(srcUrl: String, hashTrack: Int, startPosition: Int) {
        Log.d(TAG, "Registering track: $hashTrack with source: $srcUrl at position: $startPosition")
        if (registeredTracks.containsKey(hashTrack)) {
            Log.w(TAG, "Track $hashTrack is already registered. Overwriting existing track.")
            return
        }
        registeredTracks[hashTrack] = TrackInfo(srcUrl, hashTrack, startPosition)
    }

    override fun clearTrack(hashTrack: Int) {
        Log.d(TAG, "Clearing track: $hashTrack")
        registeredTracks.remove(hashTrack)
        stateListeners.remove(hashTrack)
        progressListeners.remove(hashTrack)
        speedListeners.remove(hashTrack)
    }

    override fun play(srcUrl: String, hashTrack: Int) {
        if (hashTrack != currentAudioHash) {
            Log.d(TAG, "Playing track: $hashTrack from source: $srcUrl")
            currentAudioHash = hashTrack
            setAudio(srcUrl, hashTrack, true)
            return
        }
        when (playerState) {
            AudioState.PAUSED -> {
                Log.d(TAG, "Resuming playback for track: $hashTrack")
                start()
            }
            AudioState.PLAYING -> {
                Log.d(TAG, "Track: $hashTrack is already playing.")
                pause()
            }
            else -> {
                Log.d(TAG, "Starting playback for track: $hashTrack")
                start()
            }
        }
    }

    override fun changeSpeed(isIncrease: Boolean) {
        Log.d(TAG, "Changing speed. Increase: $isIncrease")
        if(!coreMediaPlayer.isSpeedChangeSupported()) {
            Log.w(TAG, "Speed change not supported in current state: $playerState")
            return
        }
        if(currentSpeed > maximumSpeed || currentSpeed < 0f) {
            Log.w(TAG, "Current speed $currentSpeed is out of bounds. Resetting to default.")
            coreMediaPlayer.speed = DEFAULT_SPEED
            return
        }
        val newSpeed = if (isIncrease) {
            (currentSpeed + speedStep).coerceAtMost(maximumSpeed)
        } else {
            (currentSpeed - speedStep).coerceAtLeast(0f)
        }
        if((playerState == AudioState.PLAYING || playerState == AudioState.PREPARED)) {
            Log.d(TAG, "Speed changed to $newSpeed")
            currentSpeed = newSpeed
            if(playerState == AudioState.PLAYING) {
                coreMediaPlayer.speed = newSpeed
            }
        }

    }

    override fun prepare(srcUrl: String, hashTrack: Int) {
        Log.d(TAG, "Preparing track: $hashTrack from source: $srcUrl")
        if (hashTrack == currentAudioHash) return
        resetMediaPlayer()
        setAudio(srcUrl, hashTrack)
    }

    override fun pause() {
        Log.d(TAG, "Pausing playback for track: $currentAudioHash")
        if (playerState != AudioState.PLAYING) {
            Log.w(TAG, "Cannot pause. Player is not in PLAYING state.")
            return
        }
        coreMediaPlayer.pause()
        seek[currentAudioHash] = coreMediaPlayer.currentPosition
        playerState = AudioState.PAUSED
        notifyStateChange(currentAudioHash, playerState)
    }

    override fun resume(hashTrack: Int) {
        Log.d(TAG, "Resuming playback for track: $currentAudioHash")
        if (currentAudioHash != hashTrack) {
            Log.w(TAG, "Cannot resume. Current track $currentAudioHash does not match requested track $hashTrack.")
            return
        }
        if (playerState != AudioState.PAUSED && playerState != AudioState.IDLE) {
            Log.w(TAG, "Cannot resume. Player is not in PAUSED state.")
            return
        }
        start()
    }

    override fun stop() {
        Log.d(TAG, "Stopping playback for track: $currentAudioHash")
        // TODO: Implement stop logic if needed
    }

    override fun seekTo(position: Long, hasTrack: Int) {
        Log.d(TAG, "Seeking to position: $position ms for track: $currentAudioHash")
        if (currentAudioHash != hasTrack) return
        seek[hasTrack] = position.toInt()
        if (coreMediaPlayer.isSeekable()) {
            coreMediaPlayer.seekTo(position)
        }
    }

    override fun currentSpeed(): Float = coreMediaPlayer.speed

    override fun currentProgress(): Long = coreMediaPlayer.currentPosition.toLong()

    override fun dispose() {
        audioScope.launch(Dispatchers.Main) {
            Log.d(TAG, "Disposing audio player.")
            stateListeners.clear()
            progressListeners.clear()
            speedListeners.clear()
            registeredTracks.clear()
            seek.clear()
            resetMediaPlayer()
            coreMediaPlayer.release()
        }
    }

    override fun removeAudio(audioHash: Int) {
        Log.d(TAG, "Removing audio track: $audioHash")
        registeredTracks.remove(audioHash)
        stateListeners.remove(audioHash)
        progressListeners.remove(audioHash)
        speedListeners.remove(audioHash)
        seek.remove(audioHash)
    }

    override fun removeListAudio(audioHashList: List<Int>) {
        Log.d(TAG, "Removing audio tracks: $audioHashList")
        audioHashList.forEach { hash -> removeAudio(hash) }
    }

    /** Starts audio playback. Assumes the media player is prepared. */
    private fun start() {
        val currentPosition = coreMediaPlayer.currentPosition
        Log.d(TAG, "Current player state: $playerState for track: $currentAudioHash")
        if (playerState == AudioState.PAUSED || playerState == AudioState.IDLE) {
            val seekTo = seek[currentAudioHash] ?: 0
            val duration = coreMediaPlayer.duration
            if (seekTo in 1 until duration) {
                Log.d(TAG, "Seeking to position: $seekTo ms after starting playback.")

                coreMediaPlayer.seekTo(seekTo.toLong())

                if(coreMediaPlayer.isSpeedChangeSupported()) {
                    coreMediaPlayer.speed = currentSpeed
                }

                coreMediaPlayer.start()

                playerState = AudioState.PLAYING
                notifyStateChange(currentAudioHash, playerState)
            } else {
                Log.d(TAG, "No valid seek position found. Continuing from current position: $currentPosition ms.")
                val progressInfo = ProgressInfo(
                    currentPosition = currentPosition.toLong(),
                    duration = duration.toLong()
                )
                onProgressChange(currentAudioHash, progressInfo)
                onComplete(currentAudioHash)
            }
        }
    }

    /** Handles media player preparation completion. */
    private fun onPrepared(hasTrack: Int, autoPlay: Boolean) {
        Log.d(TAG, "Media player prepared for track: $hasTrack. AutoPlay: $autoPlay")
        playerState = AudioState.IDLE
        notifyStateChange(hasTrack, playerState)
        if (this.autoPlay) start()
    }

    /** Resets the media player to its initial state. */
    private fun resetMediaPlayer() {
        Log.d(TAG, "Resetting media player.")
        coreMediaPlayer.reset()
        playerState = AudioState.UNSET
        if (currentAudioHash != -1) {
            notifyStateChange(currentAudioHash, AudioState.UNSET)
            currentAudioHash = -1
        }
    }

    /** Sets the audio source for playback. */
    private fun setAudio(srcUrl: String, hash: Int, autoPlay: Boolean = true) {
        Log.d(TAG, "Setting audio source: $srcUrl for track: $hash with autoPlay: $autoPlay")
        currentAudioHash = hash
        try {
            coreMediaPlayer.run {
                setOnPreparedListener { onPrepared(hash, autoPlay) }
                setOnCompletionListener { complete(hash) }
                setOnErrorListener { _, _ ->
                    Log.e(TAG, "Media player error for track: $hash")
                    true
                }
                playerState = AudioState.PREPARED
                notifyStateChange(hash, playerState)
                setSource(srcUrl)
                prepareSync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting audio source: $srcUrl for track: $hash", e)
            playerState = AudioState.IDLE
        }
    }

    /** Notifies registered listeners about a state change for a specific track. */
    private fun notifyStateChange(hashTrack: Int, newState: AudioState) {
        stateListeners[hashTrack]?.invoke(newState)
    }

    /** Handles audio playback completion. */
    private fun onComplete(hashTrack: Int) {
        Log.d(TAG, "Audio playback completed for track: $currentAudioHash")
        audioScope.launch { complete(hashTrack) }
    }

    /** Completes the audio playback for a specific track. */
    private fun complete(hashTrack: Int) {
        Log.d(TAG, "Audio track $hashTrack marked as completed.")
        onProgressChange(hashTrack, ProgressInfo(0, 0))
        playerState = AudioState.COMPLETED
        notifyStateChange(hashTrack, playerState)
        seek[hashTrack] = 0
    }
}