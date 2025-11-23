package com.example.fluentread.service.media.audio.player

import android.media.MediaPlayer
import android.util.Log
import java.io.IOException

enum class CoreMediaState {
    IDLE,
    INITIALIZED,
    PREPARING,
    PREPARED,
    STARTED,
    PAUSED,
    STOPPED,
    COMPLETED,
    END,
}

interface CoreMediaPlayer {
    companion object {
        // Unspecified media player error.
        const val MEDIA_ERROR_UNKNOWN = 1

        // Media server died. The app must release and recreate the player.
        const val MEDIA_ERROR_SERVER_DIED = 100

        // File is not valid for progressive playback.
        const val MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200

        // File or network related operation error.
        const val MEDIA_ERROR_IO = -1004

        // Bitstream is malformed or not conforming to the standard.
        const val MEDIA_ERROR_MALFORMED = -1007

        // Bitstream is valid but unsupported by the media framework.
        const val MEDIA_ERROR_UNSUPPORTED = -1010

        // Operation timed out, usually due to network issues.
        const val MEDIA_ERROR_TIMED_OUT = -110

        // --- MEDIA_INFO constants (informational, not errors) ---

        // Unspecified media player info.
        const val MEDIA_INFO_UNKNOWN = 1

        // Buffering has started.
        const val MEDIA_INFO_BUFFERING_START = 701

        // Buffering has ended.
        const val MEDIA_INFO_BUFFERING_END = 702

        // Video rendering has started.
        const val MEDIA_INFO_VIDEO_RENDERING_START = 3

        // Audio rendering has started.
        const val MEDIA_INFO_AUDIO_RENDERING_START = 4

        // File has bad interleaving.
        const val MEDIA_INFO_BAD_INTERLEAVING = 800

        // Media is not seekable.
        const val MEDIA_INFO_NOT_SEEKABLE = 801

        // Metadata has been updated.
        const val MEDIA_INFO_METADATA_UPDATE = 802
    }

    /**
     * Checks if the media player supports seeking.
     * @return True if the media player is seekable, false otherwise.
     */
    fun isSeekable(): Boolean

    /**
     * Checks if the media player supports changing playback speed.
     * @return True if speed change is supported, false otherwise.
     */
    fun isSpeedChangeSupported(): Boolean


    /**
     * Gets or sets the playback speed of the media player.
     * @throws IllegalStateException if the media player is not in a valid state to get or set the speed.
     */
    @get:Throws(IllegalStateException::class)
    var speed: Float

    /**
     * Gets the current playback position in milliseconds.
     */
    val currentPosition: Int

    /**
     * Gets the current state of the media player.
     */
    var state: CoreMediaState

    /**
     * Gets the total duration of the media in milliseconds.
     */
    val duration: Int


    /**
     * Sets the data source for the media player.
     * @param srcUrl The source URL of the media.
     */
    @Throws(
        IllegalStateException::class,
        IOException::class,
        SecurityException::class
    )
    fun setSource(srcUrl: String): Unit

    /**
     * Prepares the media player for playback.
     *
     * after setting the data source and displaying the media. call this method to prepare the player for playback.
     */
    @Throws(IllegalStateException::class, IOException::class)
    fun prepare(): Unit


    /**
     * Prepares the media player for playback asynchronously.
     *
     * after setting the data source and displaying the media. call this method to prepare the player for playback.
     */
    @Throws(IllegalStateException::class)
    fun prepareSync(): Unit

    /**
     * Starts or resumes playback of the media.
     */
    @Throws(IllegalStateException::class)
    fun seekTo(position: Long): Unit

    /**
     * Starts or resumes playback of the media.
     */
    @Throws(IllegalStateException::class)
    fun start(): Unit

    /**
     * Pauses playback of the media.
     */
    @Throws(IllegalStateException::class)
    fun pause(): Unit

    /**
     * Stops playback of the media.
     */
    @Throws(IllegalStateException::class)
    fun stop(): Unit

    /**
     * Releases resources associated with the media player.
     */
    fun release(): Unit

    /**
     * Resets the media player to its uninitialized state.
     */
    fun reset(): Unit


    /**
     * Sets a listener for error events during playback.
     */
    fun setOnErrorListener(listener: (Int, Int) -> Boolean)

    /**
     * Sets a listener for completion events during playback.
     */
    fun setOnCompletionListener(listener: () -> Unit)

    /**
     * Sets a listener for prepared events during playback.
     */
    fun setOnPreparedListener(listener: () -> Unit)

}


class CoreMediaPlayerImpl(
    val mediaBuilder: () -> MediaPlayer
) : CoreMediaPlayer {

    companion object {
        private const val TAG = "CoreMediaPlayerImpl"
    }

    private var _mediaPlayer: MediaPlayer? = null
        set(value) {
            Log.d(TAG, "Setting MediaPlayer instance.")
            field = value
        }

    private val mediaPlayer: MediaPlayer get () {
        return _mediaPlayer ?: mediaBuilder().also {
            Log.d(TAG, "Creating new MediaPlayer instance.")
            _mediaPlayer = it.setListeners()
            state = CoreMediaState.IDLE
        }
    }

    override var speed: Float
        get() = mediaPlayer.playbackParams.speed
        set(value) {
            mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(value)
        }

    override val currentPosition: Int
        get() = mediaPlayer.currentPosition

    override val duration: Int
        get() = mediaPlayer.duration

    override fun isSeekable(): Boolean {
        return when(state) {
            CoreMediaState.PREPARED,
            CoreMediaState.STARTED,
            CoreMediaState.PAUSED,
            CoreMediaState.COMPLETED -> true
            else -> false
        }
    }

    override fun isSpeedChangeSupported(): Boolean {
        return when(state) {
            CoreMediaState.PREPARED,
            CoreMediaState.PAUSED,
            CoreMediaState.IDLE,
            CoreMediaState.STARTED -> true
            else -> false
        }
    }

    override var state: CoreMediaState = CoreMediaState.END
        get() {
            // Note: MediaPlayer does not provide a direct way to get its state.
            // This is a simplified approximation based on common states.
            return when {
                !mediaPlayer.isPlaying && mediaPlayer.currentPosition == 0 -> CoreMediaState.IDLE
                mediaPlayer.isPlaying -> CoreMediaState.STARTED
                else -> CoreMediaState.PAUSED
            }
        }
        set(value) {
            Log.d(TAG, "Setting media player state to: $value")
            field = value
        }
    private val _onErrorListener: MediaPlayer.OnErrorListener = MediaPlayer.OnErrorListener {
        _, what, extra ->
        onErrorListener?.invoke(what, extra) == true
    }

    private val _onCompletionListener: MediaPlayer.OnCompletionListener = MediaPlayer.OnCompletionListener {
        state = CoreMediaState.COMPLETED
        onCompletionListener?.invoke()
    }

    private val _onPreparedListener: MediaPlayer.OnPreparedListener = MediaPlayer.OnPreparedListener {
        state = CoreMediaState.PREPARED
        onPreparedListener?.invoke()
    }

    private var onErrorListener: ((what: Int, extra: Int) -> Boolean)? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var onPreparedListener: (() -> Unit)? = null
    private var onPollAmplitudeListener: ((amplitude: Float) -> Unit)? = null


    @Throws(
        IllegalStateException::class,
        IOException::class,
        SecurityException::class
    )
    override fun setSource(srcUrl: String) {
        try{
            Log.d(TAG, "Setting media source to: $srcUrl")
            mediaPlayer.setDataSource(srcUrl)
            state = CoreMediaState.INITIALIZED
        } catch (e: Exception) {
            Log.e(TAG, "Error setting media source: ${e.message}")
            throw e
        }
    }

    @Throws(IllegalStateException::class, IOException::class)
    override fun prepare() {
        Log.d(TAG, "Preparing media player.")
        mediaPlayer.prepare()
        state = CoreMediaState.PREPARED
    }

    @Throws(IllegalStateException::class)
    override fun prepareSync() {
        runCatching {
            Log.d(TAG, "Preparing media player asynchronously.")
            mediaPlayer.prepareAsync()
            state = CoreMediaState.PREPARING
        }.onFailure {
            Log.e(TAG, "Error preparing media player asynchronously: ${it.message}")
            throw it
        }
    }

    @Throws(IllegalStateException::class)
    override fun seekTo(position: Long) {
        Log.d(TAG, "Seeking to position: $position ms")
        mediaPlayer.seekTo(position.toInt())

    }

    @Throws(IllegalStateException::class)
    override fun start() {
        Log.d(TAG, "Starting media playback.")
        mediaPlayer.start()
        state = CoreMediaState.STARTED
    }

    @Throws(IllegalStateException::class)
    override fun pause() {
        Log.d(TAG, "Pausing media playback.")
        mediaPlayer.pause()
        state = CoreMediaState.PAUSED
    }

    @Throws(IllegalStateException::class)
    override fun stop() {
        Log.d(TAG, "Stopping media playback.")
        mediaPlayer.stop()
        state = CoreMediaState.STOPPED
    }

    override fun release() {
        Log.d(TAG, "Releasing media player resources.")
        mediaPlayer.release()
        state = CoreMediaState.IDLE
    }

    override fun reset() {
        Log.d(TAG, "Resetting media player.")
        mediaPlayer.clearAllListener().reset()
        state = CoreMediaState.IDLE
    }

    override fun setOnErrorListener(listener: (Int, Int) -> Boolean) {
        this.onErrorListener = listener
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        this.onCompletionListener = listener
    }

    override fun setOnPreparedListener(listener: () -> Unit) {
        this.onPreparedListener = listener
    }
    // --- Private Helpers ---
    /**
     * Sets up the MediaPlayer listeners for error, prepared, and completion events.
     */
    private fun MediaPlayer.setListeners(): MediaPlayer {
        Log.d(TAG, "Setting up MediaPlayer listeners.")
        setOnErrorListener(_onErrorListener)
        setOnPreparedListener(_onPreparedListener)
        setOnCompletionListener(_onCompletionListener)
        return this
    }

    /**
     * Clears all listeners from the MediaPlayer to prevent memory leaks.
     */
    private fun MediaPlayer.clearAllListener(): MediaPlayer {
        setOnErrorListener(null)
        setOnPreparedListener(null)
        setOnCompletionListener(null)
        return this
    }
}