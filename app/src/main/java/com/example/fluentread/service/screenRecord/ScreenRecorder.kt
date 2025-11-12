package com.example.fluentread.service.screenRecord

import android.app.*
import android.content.Intent
import android.media.*
import android.media.projection.*
import android.os.Binder
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

/**
 * Foreground service for screen recording.
 */
class ScreenRecorder : Service(), AppScreenRecorder {

    companion object {
        const val TAG = "ScreenRecorder"
        const val SCREEN_RECORD_PERMISSION_CODE = 0x1234
        const val NOTIFICATION_ID = 0x3001
        const val NOTIFICATION_CHANNEL_ID = "screen_record_channel_id"
        const val NOTIFICATION_CHANNEL_NAME = "Screen Recording"

        const val REQUEST_CODE_SCREEN_CAPTURE = 0x2001

        const val REQUEST_CODE_PERMISSION = 0

        @Volatile
        var INSTANCE: ScreenRecorder? = null
        fun getInstance(): ScreenRecorder? = INSTANCE
    }

    private val localBinder: LocalBinder = LocalBinder()

    // MediaRecorder and related resources
    private var mediaRecorder: MediaRecorder? = null
        set(value) {
            value?.apply {
                onErrorListener?.let { setOnErrorListener(it) }
                onInfoListener?.let { setOnInfoListener(it) }
            }
            field = value
        }
    private var outPutFile: File? = null
    private var mediaProjection: MediaProjection? = null
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjectionCallBack: MediaProjection.Callback? = null

    // Recording state and timing
    private var recordingStartTime: Long? = null
    private var pauseStartTime: Long? = null
    private var totalPauseDuration: Long = 0L
    private var screenRecordState: ScreenRecordState = ScreenRecordState.IDLE
        set(value) {
            field = value
            when (value) {
                ScreenRecordState.IDLE -> resetTiming()
                ScreenRecordState.RECORDING -> startTiming()
                ScreenRecordState.PAUSED -> pauseTiming()
                else -> Unit
            }
            Log.d(TAG, "MediaRecorder instance updated. $value")
        }

    // Notification
    private var notificationBuilder: NotificationCompat.Builder? = null
    private val notificationHelper by lazy {
        NotificationHelper(this, NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME)
    }
    private var notificationChannel: NotificationChannel? = null

    // Listeners
    private var onErrorListener: MediaRecorder.OnErrorListener? = null
    private var onInfoListener: MediaRecorder.OnInfoListener? = null
    private var onMediaProjectionStopListener: AppScreenRecorder.OnMediaProjectionStopListener? = null
    private var onCapturedContentListener: AppScreenRecorder.OnCapturedContentListener? = null
    private var onCaptureContentVisibilityListener: AppScreenRecorder.OnCaptureContentVisibilityListener? = null

    // Coroutine scope for background tasks
    private val recordCoroutine = CoroutineScope(Dispatchers.IO)

    // State helpers
    private var isInitialMedia: Boolean = false
    private val recordAvailable: Boolean get() = mediaProjection != null && outPutFile != null

    override fun onBind(intent: Intent?): IBinder? = localBinder

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    /** Handle start, stop, and cancel actions from intents. Received action from RecordingActivity */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null) {
            when(intent.action) {
                RecordingActivity.ACTION_START -> {
                    Log.d(TAG, "Received start action")

                    startNotificationForeground()
                    // Get recording name from intent extras
                    val recordingName = intent.getStringExtra("recording_name") ?: "screen_recording_${System.currentTimeMillis()}.mp4"
                    startRecording(recordingName, intent)
                    return START_STICKY
                }
                RecordingActivity.ACTION_STOP -> {
                    Log.d(TAG, "Received stop action")
                    stopRecording()
                    stopSelf()
                }
                RecordingActivity.ACTION_CANCEL -> {
                    Log.d(TAG, "Received cancel action")
                    release()
                    destroyMediaProjection()
                    stopSelf()
                }
                else -> Log.w(TAG, "Unknown action received: ${intent.action}")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }


    override fun isRecording(): Boolean = screenRecordState == ScreenRecordState.RECORDING

    /**
     * Binder class for clients to interact with the service.Return instance of ScreenRecorder Service
     * Example: val getService = (binder as ScreenRecorder.LocalBinder).getService()
     * */
    inner class LocalBinder : Binder() {
        fun getService(): ScreenRecorder = this@ScreenRecorder
    }

    /** Build MediaRecorder instance based on Android version. */
    fun buildMediaRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

    /**
     * Initialize MediaProjection and MediaRecorder for screen recording.
     */
    @Throws
    private fun initMediaProjection(savedFile: File) {
        Log.d(TAG, "Initializing MediaProjection and MediaRecorder. File path: ${savedFile.absolutePath}")

        release()
        mediaRecorder = buildMediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(savedFile.absolutePath)
            setVideoEncodingBitRate(8 * 1000 * 1000)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoSize(1920, 1080)
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
        if (isRecording() || mediaProjectionManager == null) {
            Log.w(TAG, "Screen recording is already in progress.")
            return
        }
        runCatching {
            FileHelper.createFileInCache(applicationContext, fileName = recordingName)?.let { file ->

                outPutFile = file
                mediaProjection = mediaProjectionManager?.getMediaProjection(Activity.RESULT_OK, data)
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
                initMediaProjection(file)
                mediaRecorder?.start()
                screenRecordState = ScreenRecordState.RECORDING
            }
        }.onFailure { err ->
            Log.e(TAG, "Error initializing MediaProjection: ${err.message}", err)
            release()
        }
    }

    override fun stopRecording(): ScreenRecordResult {
        if (!isRecording()) {
            Log.w(TAG, "No active screen recording to stop.")
            throw IllegalStateException("No active screen recording to stop.")
        }
        return runCatching {
            val duration = calculateDuration()
            release()
            destroyMediaProjection()

            val recordPath = FileHelper.saveVideoToMediaStore(applicationContext, outPutFile)

            @Suppress("DEPRECATION")
            stopForeground(true)
            val result = ScreenRecordResult(
                filePath = recordPath ?: outPutFile?.absolutePath.orEmpty(),
                durationMillis = duration,
                fileSizeBytes = outPutFile!!.length()
            )
            Log.d(TAG, "Screen recording stopped. File: ${result.filePath}, Duration: ${result.durationMillis} ms, Size: ${result.fileSizeBytes} bytes")
            outPutFile = null
            result
        }.getOrElse { err ->
            Log.e(TAG, "Error stopping screen recording: ${err.message}", err)
            throw err
        }
    }

    override fun pause() {
        mediaRecorder?.pause()
        screenRecordState = ScreenRecordState.PAUSED
    }

    override fun resume() {
        mediaRecorder?.resume()
        screenRecordState = ScreenRecordState.RECORDING
    }

    override fun setOnErrorListener(listener: AppScreenRecorder.OnErrorListener) {
        onErrorListener = MediaRecorder.OnErrorListener { _, what, extra ->
            listener.onError(what, extra)
        }
        mediaRecorder?.setOnErrorListener(onErrorListener)
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

    /** Start foreground notification for recording. */
    private fun startNotificationForeground() {
        runCatching {
            notificationBuilder = notificationHelper.initNotificationBuilder(
                contentText = "Screen recording in progress",
                contentTitle = "FluentRead Screen Recorder"
            )
            notificationChannel = notificationHelper.createNotificationChannel()
            startForeground(NOTIFICATION_ID, notificationBuilder!!.build())
        }.onFailure {
            Log.e(TAG, "Failed to start foreground notification: ${it.message}", it)
        }
    }

    /** Release MediaRecorder and MediaProjection resources. */
    private fun release() {
        mediaRecorder?.runCatching {
            try {
                stop()
            } catch (e: RuntimeException) {
                Log.e(TAG, "Error stopping MediaRecorder: ${e.message}", e)
                outPutFile?.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error stopping MediaRecorder: ${e.message}", e)
            } finally { release() }
        }
        screenRecordState = ScreenRecordState.IDLE
        mediaRecorder = null
    }

    /** Destroy MediaProjection and unregister callback. */
    private fun destroyMediaProjection() {
        mediaProjectionCallBack?.let { mediaProjection?.unregisterCallback(it) }
        mediaProjectionCallBack = null
        mediaProjection?.stop()
        mediaProjection = null
    }

    /** Calculate recording duration. */
    private fun calculateDuration(): Long {
        val elapsed = recordingStartTime?.let { System.currentTimeMillis() - it - totalPauseDuration }
        val mediaDuration = getMediaDuration(outPutFile)
        Log.d(TAG, "Calculated duration: $elapsed ms, Media duration: $mediaDuration ms")
        return elapsed?.takeIf { it > 0 } ?: mediaDuration
    }

    /**
     * Get duration of media file in milliseconds.
     */
    private fun getMediaDuration(file: File?): Long {
        file ?: return 0
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.toURI().toString())
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e(AudioMediaRecorder.TAG, "Failed to get media duration: ${e.message}", e)
            0L
        } finally {
            try {
                retriever.release()
            } catch (error: Throwable) {
                Log.e(AudioMediaRecorder.TAG, "Failed to release MediaMetadataRetriever: ${error.message}", error)
            }
        }
    }

    /** Reset timing for IDLE state. */
    private fun resetTiming() {
        recordingStartTime = null
        pauseStartTime = null
        totalPauseDuration = 0L
    }

    /** Start timing for RECORDING state. */
    private fun startTiming() {
        if (recordingStartTime == null) recordingStartTime = System.currentTimeMillis()
        pauseStartTime?.let {
            totalPauseDuration += System.currentTimeMillis() - it
            pauseStartTime = null
        }
    }

    /** Pause timing for PAUSED state. */
    private fun pauseTiming() {
        pauseStartTime = System.currentTimeMillis()
    }
}