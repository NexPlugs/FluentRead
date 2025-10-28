package com.example.fluentread.service.audio.player

import java.io.IOException

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
     * Sets a listener for error events during playback.
     */
    fun setOnErrorListener(listener: (Int, Int) -> Unit): Unit
}

class CoreMediaPlayerImpl : CoreMediaPlayer {
    override var speed: Float
        get() = TODO("Not yet implemented")
        set(value) {}
    override val currentPosition: Int
        get() = TODO("Not yet implemented")
    override val duration: Int
        get() = TODO("Not yet implemented")

    override fun setSource(srcUrl: String) {
        TODO("Not yet implemented")
    }

    override fun prepare() {
        TODO("Not yet implemented")
    }

    override fun seekTo(position: Long) {
        TODO("Not yet implemented")
    }

    override fun start() {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override fun setOnErrorListener(listener: (Int, Int) -> Unit) {
        TODO("Not yet implemented")
    }
}