package com.example.fluentread.service.screenRecord

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.*
import android.media.projection.*
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import com.example.fluentread.MainActivity
import com.example.fluentread.R
import com.example.fluentread.service.file.FileHelper
import com.example.fluentread.service.file.MediaType
import com.example.fluentread.service.notification.NotificationHelper
import com.example.fluentread.service.overlay.ToggleView
import com.example.fluentread.service.overlay.screenRecordCompose.ScreenRecordCompose
import com.example.fluentread.service.overlay.screenRecordCompose.ScreenRecordViewModel
import com.example.fluentread.service.screenRecord.models.ScreenRecordConfig
import com.example.fluentread.service.screenRecord.models.ScreenRecordResult
import com.example.fluentread.service.screenRecord.models.ScreenRecordState
import com.example.fluentread.utils.parcelable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Foreground service for screen recording.
 */
@AndroidEntryPoint
class ScreenRecorder : Service(), AppScreenRecorder {

    companion object {
        const val TAG = "ScreenRecorder"

        const val NOTIFICATION_ID = 0x3001
        const val NOTIFICATION_CHANNEL_ID = "screen_record_channel_id"
        const val NOTIFICATION_CHANNEL_NAME = "Screen Recording"


        const val SCREEN_RECORD_CONFIG = "screen_record_config"

        @Volatile
        var INSTANCE: ScreenRecorder? = null
        fun getInstance(): ScreenRecorder? = INSTANCE
    }

    /** Screen recorder toggle */
    @Inject lateinit var screenRecordViewModel: ScreenRecordViewModel
    private var screenRecordToggle : ToggleView? = null

    private val localBinder: LocalBinder = LocalBinder()
    private val localBroadcastReceiver: LocalBroadcastReceiver = LocalBroadcastReceiver()
    private var mediaPermission: Intent? = null


    // MediaRecorder and related resources
    private var mediaRecorder: MediaRecorder? = null
        set(value) {
            value?.apply {
                onErrorListener?.let { value.setOnErrorListener(it) }
                onInfoListener?.let { value.setOnInfoListener(it) }
            }
            field = value
        }
    private var outPutFile: File? = null
    private var mediaProjection: MediaProjection? = null
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjectionCallBack: MediaProjection.Callback? = null
    private var virtualDisplay: VirtualDisplay? = null
    private lateinit var displayMetrics: DisplayMetrics
    private lateinit var windowManager: WindowManager

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
    private val recordCoroutine = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // State helpers
    private var isInitialMedia: Boolean = false

    // region === Configuration ===
    private var screenRecordConfig: ScreenRecordConfig = ScreenRecordConfig()
    private val enableAudio: Boolean
        get() = screenRecordConfig.isAudioEnable
    // endregion

    override fun onBind(intent: Intent?): IBinder? = localBinder

    override fun onCreate() {
        super.onCreate()

        val context = this.applicationContext

        Log.d(TAG, "onCreate: ScreenRecorder Service created")
        INSTANCE = this
        mediaProjectionManager = context.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        displayMetrics = resources.displayMetrics

        // NOTE: getRealMetrics is deprecated in API 30, but still used for compatibility
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)

        // Init local broadcast receiver for handling system events
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SHUTDOWN)
            addAction(Intent.ACTION_DELETE)
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(localBroadcastReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            registerReceiver(localBroadcastReceiver, intentFilter)
        }
    }

    /** Handle start, stop, and cancel actions from intents. Received action from RecordingActivity */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null) {
            when(intent.action) {
                ScreenRecordActivity.ACTION_START -> {
                    Log.d(TAG, "Received start action")

                    mediaPermission = intent.parcelable(Intent.EXTRA_INTENT)
                    screenRecordConfig = intent.parcelable(SCREEN_RECORD_CONFIG) ?: ScreenRecordConfig()

                    startNotificationForeground()
                    onCreateAndShowScreenRecordToggle()

                    return START_STICKY
                }
                ScreenRecordActivity.ACTION_STOP -> {
                    Log.d(TAG, "Received stop action")
                    stopRecording()
                    stopSelf()
                }
                ScreenRecordActivity.ACTION_CANCEL -> {
                    Log.d(TAG, "Received cancel action")
                }
                else -> Log.w(TAG, "Unknown action received: ${intent.action}")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /** Create and show the screen record toggle bubble */
    private fun onCreateAndShowScreenRecordToggle() {
        if(screenRecordToggle != null) return
        if(!Settings.canDrawOverlays(this)) return
        Log.d(TAG, "onCreateScreenRecordToggle: Creating screen record toggle bubble")
        screenRecordToggle = ToggleView(context = this, startPoint = Point(0, 300)).apply {
            rootGroup?.addView(ComposeView(context).apply {
                setContent {
                    ScreenRecordCompose(
                        screenRecordViewModel = screenRecordViewModel,
                        onStartRecording = {},
                        onStopRecording = {},
                    )
                }
            })
        }
        screenRecordToggle?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        release()

        recordCoroutine.cancel()
        unregisterReceiver(localBroadcastReceiver)
    }


    override fun isRecording(): Boolean = screenRecordState == ScreenRecordState.RECORDING

    /**
     * Binder class for clients to interact with the service.Return instance of ScreenRecorder Service
     * Example: val getService = (binder as ScreenRecorder.LocalBinder).getService()
     * */
    inner class LocalBinder : Binder() {
        fun getService(): ScreenRecorder = this@ScreenRecorder
    }

    /** Local broadcast receiver placeholder. */
    private inner class LocalBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            when(intent.action) {
                // If behavior requires stopping recording on screen off or shutdown
                Intent.ACTION_SHUTDOWN -> {
                    Log.d(TAG, "Screen turned off. Stopping recording if active.")
                    stopRecording()
                }
                Intent.ACTION_DELETE -> {
                    Log.d(TAG, "Recording file deleted: ${intent.data}. Stopping recording if active.")
                }
            }
        }

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
    private fun initMediaRecorder(savedFile: File) {
        Log.d(TAG, "Initializing MediaProjection and MediaRecorder. File path: ${savedFile.absolutePath}")

        release()

        Log.d(TAG, "ScreenRecordConfig: $screenRecordConfig")
        try {
            mediaRecorder = buildMediaRecorder().apply {
                /** Config media source */
                setVideoSource(screenRecordConfig.videoSource)
                if(enableAudio) {
                    setAudioSource(screenRecordConfig.audioSource)
                }

                setOutputFormat(screenRecordConfig.outputFormat)
                setOutputFile(savedFile.absolutePath)
                setVideoEncodingBitRate(screenRecordConfig.videoEncodingBitRate)
                setVideoSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
                setVideoFrameRate(screenRecordConfig.videoFrameRate)
                setVideoEncoder(screenRecordConfig.videoEncoder)
                if(enableAudio) {
                    setAudioEncoder(screenRecordConfig.audioEncoder)
                    setAudioEncodingBitRate(screenRecordConfig.audioEncodingBitRate)
                    setAudioSamplingRate(screenRecordConfig.audioSamplingRate)
                }
            }
            mediaRecorder?.setOnErrorListener {
                what, extra, _ ->
                Log.e(TAG, "MediaRecorder error occurred. What: $what, Extra: $extra")
            }
            mediaRecorder?.prepare()

        } catch (e: Error) {
            Log.e(TAG, "Error preparing MediaRecorder: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception preparing MediaRecorder: ${e.message}", e)
        }
        screenRecordState = ScreenRecordState.PREPARED
        isInitialMedia = true
    }

    override fun startRecording(recordingName: String?) {
        val name = recordingName ?: "screen_recording_${System.currentTimeMillis()}.mp4"
        recordCoroutine.launch {
            startRecordingInternal(name)
        }
    }

    /** Start screen recording with the given name. */
    private fun startRecordingInternal(recordingName: String) {
        mediaPermission ?: return
        if (isRecording() || mediaProjectionManager == null) {
            Log.w(TAG, "Screen recording is already in progress.")
            return
        }
        recordCoroutine.launch {
            runCatching {
                FileHelper.createFileInCache(applicationContext, fileName = recordingName)?.let { file ->
                    outPutFile = file
                    mediaProjection = mediaProjectionManager?.getMediaProjection(Activity.RESULT_OK, mediaPermission!!)
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
                    initMediaRecorder(file)
                    virtualDisplay = createVirtualDisplay()
                    mediaRecorder?.start()
                    screenRecordState = ScreenRecordState.RECORDING
                }
            }.onFailure { err ->
                Log.e(TAG, "Error initializing MediaProjection: ${err.message}", err)
                release()
            }
        }
    }


    /** Create virtual display for screen recording. */
    private fun createVirtualDisplay(): VirtualDisplay? {
        mediaProjection ?: return null
        return mediaProjection!!.createVirtualDisplay(
            TAG, displayMetrics.widthPixels,
            displayMetrics.heightPixels, displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder?.surface, null, null
        )
    }

    override fun stopRecording() {
        recordCoroutine.launch { stopRecordingInternal() }
    }


    /** Stop screen recording and return result. */
    private fun stopRecordingInternal() : ScreenRecordResult{

        if (!isRecording()) {
            Log.w(TAG, "No active screen recording to stop.")
            throw IllegalStateException("No active screen recording to stop.")
        }
        return runCatching {
            val duration = calculateDuration()
            release()

            val recordPath = FileHelper.saveFileToMediaStore(applicationContext, outPutFile, mediaType = MediaType.VIDEO)

            stopNotificationForeground()

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
            val intent = Intent(this, ScreenRecorder::class.java).apply {
                action = ScreenRecordActivity.ACTION_STOP
            }

            val stopPendingIntent = PendingIntent.getService(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val dismissIntent = Intent(this, ScreenRecorder::class.java).apply {
                action = ScreenRecordActivity.ACTION_CANCEL
            }

            val cancelPending = PendingIntent.getService(
                this, 0, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            // Open main application
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val mainPendingIntent = PendingIntent.getActivity(
                this, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


            notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setColor(Color.RED)
                .setContentTitle("Screen Recording")
                .setContentText("Screen recording is in progress")
                .setSmallIcon(R.drawable.ic_record)
                .setContentIntent(mainPendingIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_up, "Stop", stopPendingIntent)
                .addAction(R.drawable.ic_down, "Cancel", cancelPending)


            notificationChannel = notificationHelper.createNotificationChannel()
            startForeground(NOTIFICATION_ID, notificationBuilder!!.build())
        }.onFailure {
            Log.e(TAG, "Failed to start foreground notification: ${it.message}", it)
        }
    }

    private fun stopNotificationForeground() {
        runCatching {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }.onFailure {
            Log.e(TAG, "Failed to stop foreground notification: ${it.message}", it)
        }
    }

    /** Release MediaRecorder and MediaProjection resources safely. */
    private fun release() {
        val recorder = mediaRecorder ?: return
        try {
            if (screenRecordState == ScreenRecordState.RECORDING) {
                Log.d(TAG, "Attempting to stop MediaRecorder safely...")

                Thread.sleep(250)

                if (mediaProjection == null || virtualDisplay?.surface == null) {
                    Log.w(TAG, "Projection or surface lost before stop(), skipping stop. $mediaProjection $virtualDisplay")
                } else {
                    try {
                        recorder.setOnErrorListener(null)
                        recorder.setOnInfoListener(null)
                        recorder.stop()
                        Log.d(TAG, "MediaRecorder stopped successfully.")
                    } catch (e: RuntimeException) {
                        Log.e(TAG, "RuntimeException stopping MediaRecorder (likely stop failed -1007): ${e.message}")
                    }
                }
            } else {
                Log.d(TAG, "MediaRecorder is not recording. Current state: $screenRecordState, skipping stop().")
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException stopping MediaRecorder: ${e.message}", e)
        } finally {
            try {
                recorder.reset()
                recorder.release()
                Log.d(TAG, "MediaRecorder reset and released.")
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaRecorder: ${e.message}", e)
            }
            mediaRecorder = null
            virtualDisplay?.release()
            virtualDisplay = null
            screenRecordState = ScreenRecordState.IDLE
            destroyMediaProjection()
        }
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
            Log.e(TAG, "Failed to get media duration: ${e.message}", e)
            0L
        } finally {
            try {
                retriever.release()
            } catch (error: Throwable) {
                Log.e(TAG, "Failed to release MediaMetadataRetriever: ${error.message}", error)
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