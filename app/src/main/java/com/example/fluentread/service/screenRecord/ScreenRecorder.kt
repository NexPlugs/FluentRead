package com.example.fluentread.service.screenRecord

import android.app.Activity
import android.app.NotificationChannel
import android.app.Service
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fluentread.service.audio.record.AudioMediaRecorder
import com.example.fluentread.service.file.FileHelper
import com.example.fluentread.service.notification.NotificationHelper
import com.example.fluentread.service.screenRecord.models.ScreenRecordResult
import com.example.fluentread.service.screenRecord.models.ScreenRecordState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlin.jvm.Throws

/**
 * Service responsible for screen recording functionality.
 */
class ScreenRecorder: Service(), AppScreenRecorder {

    companion object {
        const val TAG = "ScreenRecorder"

        const val SCREEN_RECORD_PERMISSION_CODE = 0x1234

        // Singleton instance of ScreenRecorder -> Integrate with Service lifecycle
        var INSTANCE: ScreenRecorder? = null

        fun getInstance(): ScreenRecorder? = INSTANCE


        // Notification constants
        const val NOTIFICATION_ID = 0x3001

        const val NOTIFICATION_CHANNEL_ID = "screen_record_channel_id"

        const val NOTIFICATION_CHANNEL_NAME = "Screen Recording"
    }

    private var mediaRecorder: MediaRecorder? = null
        set(value) {
            if(value != null) {
                onErrorListener?.let { value.setOnErrorListener(it) }
                onInfoListener?.let { value.setOnInfoListener(it) }
            }
            field = value
        }

    /** Output file for the recorded screen video */
    private var outPutFile: File? = null

    /** MediaProjection instance for capturing the screen */
    private var mediaProjection: MediaProjection? = null

    /** MediaProjectionManager for managing screen capture sessions */
    private var mediaProjectionManager: MediaProjectionManager? = null

    /** Callback for MediaProjection events */
    private var mediaProjectionCallBack: MediaProjection.Callback? = null

    /** Timestamps for recording management */
    private var recordingStartTime: Long? = null
    private var pauseStartTime : Long? = null
    private var totalPauseDuration: Long = 0L


    // Notification valuable for foreground service
    /** Notification builder for foreground service notifications */
    private var notificationBuilder: NotificationCompat.Builder? = null

    private val notificationHelper: NotificationHelper get() = NotificationHelper(this, NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME)

    private var notificationChannel: NotificationChannel? = null

    /** Current state of the screen recorder */
    private var screenRecordState: ScreenRecordState = ScreenRecordState.IDLE
        set(value)  {
            field = value
            when(value) {
                ScreenRecordState.IDLE -> {
                    recordingStartTime = null
                    pauseStartTime = null
                    totalPauseDuration = 0L
                }
                ScreenRecordState.RECORDING -> {
                    if(recordingStartTime == null) {
                        recordingStartTime = System.currentTimeMillis()
                    }
                    pauseStartTime?.let {
                        totalPauseDuration += System.currentTimeMillis() - it
                        pauseStartTime = null
                    }
                }
                ScreenRecordState.PAUSED -> {
                    pauseStartTime = System.currentTimeMillis()
                }

                else -> { /* No action needed for other states */ }
            }
        }

    override fun isRecording(): Boolean = screenRecordState == ScreenRecordState.RECORDING


    /** Indicates whether MediaProjection has been initialized */
    private var isInitialMedia: Boolean = false

    /** Indicates whether recording resources are available */
    private val recordAvailable: Boolean get() = mediaProjection != null && outPutFile != null

    private var onErrorListener: MediaRecorder.OnErrorListener? = null
    private var onInfoListener: MediaRecorder.OnInfoListener? = null

    /** MediaProjectionCallBack events  */
    private var onMediaProjectionStopListener: AppScreenRecorder.OnMediaProjectionStopListener? = null
    private var onCapturedContentListener: AppScreenRecorder.OnCapturedContentListener? = null
    private var onCaptureContentVisibilityListener: AppScreenRecorder.OnCaptureContentVisibilityListener? = null

    private val recordCoroutine: CoroutineScope = CoroutineScope(Dispatchers.IO)


    // Not implement
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotificationForeground()

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        mediaProjectionManager = this.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    }

    /** Builds and returns a MediaRecorder instance, considering the Android version. */
    fun buildMediaRecorder(): MediaRecorder {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return MediaRecorder(this.applicationContext)
        }
        @Suppress("DEPRECATION")
        return MediaRecorder()
    }


    /**
     * Initializes the MediaProjection for screen recording.
     */
    @Throws
    private fun initMediaProjection(savedFile: File) {
        release()

        mediaRecorder = buildMediaRecorder().apply {
            // Maybe create a Config class for MediaRecorder settings
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(savedFile.absolutePath)
            setVideoEncodingBitRate(8 * 1000 * 1000)

            // Set Video Configurations
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoSize(1920, 1080)

            // If Enable Audio Recording, set audio configurations
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(320 * 1000)
            setAudioSamplingRate(48000)

            setVideoFrameRate(30)

            prepare()
        }

        screenRecordState = ScreenRecordState.PREPARED

        isInitialMedia = true
    }

    override fun startRecording(recordingName: String, data: Intent) {
        if(screenRecordState == ScreenRecordState.RECORDING) {
            Log.w(TAG, "startRecording: Screen recording is already in progress.")
            return
        }

        runCatching {
            FileHelper.createFileInCache(
                this.applicationContext,
                fileName = recordingName
            )?.let {
                outPutFile = it

                // Init MediaProjection and callBack
                mediaProjection = mediaProjectionManager?.getMediaProjection(Activity.RESULT_OK, data)

                // Register CallBack for MediaProjection
                mediaProjectionCallBack = object : MediaProjection.Callback() {
                    override fun onStop() {
                        super.onStop()
                        onMediaProjectionStopListener?.onStop()
                    }

                    override fun onCapturedContentResize(width: Int, height: Int) {
                        super.onCapturedContentResize(width, height)
                        onCapturedContentListener?.onCapturedContent(width, height)
                    }

                    override fun onCapturedContentVisibilityChanged(isVisible: Boolean) {
                        super.onCapturedContentVisibilityChanged(isVisible)
                        onCaptureContentVisibilityListener?.onContentVisibilityChanged(isVisible)
                    }
                }

                mediaProjection?.registerCallback(mediaProjectionCallBack!!, null)
                initMediaProjection(outPutFile!!)
                mediaRecorder?.start()
                screenRecordState = ScreenRecordState.RECORDING

            }

        }.getOrElse { err ->
            Log.d(TAG, "initMediaProjection: Error initializing MediaProjection: ${err.message}")
            release()
        }
    }

    /** Releases resources associated with the screen recorder. */
    private fun release() {
        if(mediaRecorder == null && mediaProjection == null) {
            return
        }
        mediaRecorder?.apply {
            stop()
            release()
        }
        screenRecordState = ScreenRecordState.IDLE
        mediaRecorder = null
    }

    /** Destroys the MediaProjection and unregisters its callback. */
    private fun destroyMediaProjection() {
        mediaProjectionCallBack?.let {
            mediaProjection?.unregisterCallback(it)
        }
        mediaProjectionCallBack = null
        mediaProjection?.stop()
        mediaProjection = null
    }

    override fun stopRecording(): ScreenRecordResult {
        if(screenRecordState != ScreenRecordState.RECORDING || !recordAvailable) {
            Log.w(TAG, "stopRecording: No active screen recording to stop.")
            throw IllegalStateException("No active screen recording to stop.")
        }
        runCatching {
            mediaRecorder?.stop()

            val calculateScreenRecordDuration = recordingStartTime?.let {
                System.currentTimeMillis() - it - totalPauseDuration
            }

            val mediaDuration = getMediaDuration(outPutFile)

            Log.d(TAG, "stopRecording: Calculated Screen Record Duration: $calculateScreenRecordDuration ms, Media Duration: $mediaDuration ms")

            val duration = calculateScreenRecordDuration?.takeIf { it > 0 } ?: mediaDuration


            release()
            destroyMediaProjection()

            @Suppress("DEPRECATION")
            stopForeground(true)

            val result = ScreenRecordResult(
                filePath = outPutFile!!.absolutePath,
                durationMillis = duration,
                fileSizeBytes = outPutFile!!.length()
            )
            outPutFile = null
            return result

        }.getOrElse { err ->
            Log.e(TAG, "stopRecording: Error stopping screen recording: ${err.message}", err)
            throw err
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }


    override fun pause() {
        mediaRecorder?.apply { pause() }
        screenRecordState = ScreenRecordState.PAUSED
    }

    override fun resume() {
        mediaRecorder?.apply { resume() }
        screenRecordState = ScreenRecordState.RECORDING
    }


    override fun setOnErrorListener(listener: AppScreenRecorder.OnErrorListener) {
        mediaRecorder?.setOnErrorListener {
                _, what, extra ->
            listener.onError(what = what, extra = extra)
        }
    }

    override fun setOnMediaProjectionStopListener(listener: AppScreenRecorder.OnMediaProjectionStopListener) {
        onMediaProjectionStopListener = listener
    }

    override fun setOnCapturedContentListener(listener: AppScreenRecorder.OnCapturedContentListener) {
        onCapturedContentListener = listener
    }

    override fun setOnCaptureContentVisibilityListener(listener: AppScreenRecorder.OnCaptureContentVisibilityListener) {
        onCaptureContentVisibilityListener = listener
    }


    /** Starts the foreground notification for the screen recording service. */
    private fun startNotificationForeground() {
        runCatching {
            notificationBuilder = notificationHelper.initNotificationBuilder(
                contentText = "Screen recording in progress",
                contentTitle = "FluentRead Screen Recorder",
            )
            notificationChannel = notificationHelper.createNotificationChannel()
            startForeground(NOTIFICATION_ID, notificationBuilder!!.build())
        }.onFailure {
            Log.e(TAG, "Failed to start foreground notification: ${it.message}", it)
        }
    }

    /**
     * Gets the duration of the audio file in milliseconds.
     * @param file The audio file.
     * return Duration in milliseconds, or 0 if the file is null or an error occurs.
     */
    private fun getMediaDuration(file: File?): Long {
        file ?: return 0
        // Use MediaMetadataRetriever to get the duration of the audio file
        val mediaRetriever: MediaMetadataRetriever = MediaMetadataRetriever()
        try {
            mediaRetriever.setDataSource(file.toURI().toString())
            val durationStr = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L
            try {
                mediaRetriever.release()
            } catch (error: Throwable) {
                Log.e(AudioMediaRecorder.Companion.TAG, "getAudioDuration: Failed to release MediaMetadataRetriever: ${error.message}", error)
            }
            return duration
        } catch (e: Exception) {
            Log.e(AudioMediaRecorder.Companion.TAG, "getAudioDuration: Failed to set data source: ${e.message}", e)
            return 0
        }
    }

}