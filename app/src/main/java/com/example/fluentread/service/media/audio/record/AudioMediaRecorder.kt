package com.example.fluentread.service.media.audio.record

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.example.fluentread.service.file.FileHelper
import com.example.fluentread.service.file.MediaType
import com.example.fluentread.service.media.audio.models.AudioConfig
import com.example.fluentread.service.media.audio.models.RecordResult
import com.example.fluentread.service.media.audio.models.getMimType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.jvm.Throws
import kotlin.math.log10


// MediaRecorder state enum
class AudioMediaRecorder(
    val context: Context,
    val audioConfig: AudioConfig = AudioConfig()
): AppMediaRecorder {

    companion object {
        const val TAG = "DefaultMediaRecorder"

        const val AUDIO_CONFIG = "AUDIO_CONFIG"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        var INSTANCE: AudioMediaRecorder? = null

        fun getInstance(): AudioMediaRecorder? = INSTANCE
    }


    /**
     * Current state of the MediaRecorder.
     */
    private var mediaRecorderState: MediaRecorderState = MediaRecorderState.IDLE
        set(value) {
            field = value
            onMediaRecorderStateChangeListener?.onStateChange(value)

            when (field) {
                MediaRecorderState.RECORDING -> {
                    recordingStartTime = System.currentTimeMillis()
                    Log.d(TAG, "Recording started at $recordingStartTime")
                    trackingMaxDuration()
                }

                else -> {
                    recordingStartTime = 0L
                }
            }
        }

    /**
     * Global valuables
     */
    private val showAmplitudePopUp: Boolean
        get() = audioConfig.showAmplitudePopUp

    /**
     * Coroutine scope for handling audio recording operations.
     */
    private val recordCoroutine: CoroutineScope = CoroutineScope(Dispatchers.IO)


    /**
     * MediaRecorder instance for audio recording.
     * The setter ensures that any existing MediaRecorder is released before assigning a new one.
     */
    private var mediaRecorder: MediaRecorder? = null
        set(value) {
            if (value != null) {
                onErrorListener?.let { value.setOnErrorListener(it) }
                onInfoListener?.let { value.setOnInfoListener(it) }
            }
            field = value
        }

    /**
     * Start time of the current recording in milliseconds.
     */
    private var recordingStartTime: Long? = null

    /**
     * File where the current recording is being saved.
     */
    private var recordingFile: File? = null

    private var trackingRecordingDurationJob: Job? = null
    private var trackingPillingJob: Job? = null

    private var pollingData = arrayListOf<Float>()

    // Initial all listener value

    /**
     * Listener for informational events from the MediaRecorder.
     */
    private var onInfoListener: MediaRecorder.OnInfoListener? = null

    /**
     * Listener for error events from the MediaRecorder.
     */
    private var onErrorListener: MediaRecorder.OnErrorListener? = null
    private var onRecordStartedListener: AppMediaRecorder.OnRecordStarted? = null
    private var onRecordStoppedListener: AppMediaRecorder.OnRecordStopped? = null
    private var onCurrentRecordDurationChangeListener: AppMediaRecorder.OnCurrentRecordDurationChange? =
        null
    private var onMediaRecorderStateChangeListener: AppMediaRecorder.OnMediaRecorderStateChange? =
        null
    private var onPollAmplitudeListener: AppMediaRecorder.OnPollAmplitudeListener? = null


    /**
     * Builds and returns a MediaRecorder instance base on android version.
     */
    fun buildMediaRecorder(): MediaRecorder {
        //If version android is S or higher, use the builder pattern to create MediaRecorder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return MediaRecorder(context)
        }
        @Suppress("DEPRECATION")
        return MediaRecorder()
    }


    /**
     * Initializes the MediaRecorder for audio recording with the specified save file.
     */
    @Throws
    private fun initialMediaRecorderForAudio(saveFile: File) {
        release()

        mediaRecorder = buildMediaRecorder().apply {
            setAudioSource(audioConfig.audioSource)
            setOutputFormat(audioConfig.outputFormat)
            setAudioEncoder(audioConfig.audiEncoder)
            setAudioSamplingRate(audioConfig.audiSamplingRate)
            setAudioEncodingBitRate(audioConfig.encodeBitRate)
            setAudioChannels(audioConfig.audiChannel)
            setOutputFile(saveFile.absolutePath)
            prepare()
            mediaRecorderState = MediaRecorderState.PREPARED
        }
    }

    /**
     * Starts audio recording and saves it to a file with the specified name.
     * @param recordingName The name of the recording file.
     * @param amplitudePollingInterval The interval for polling amplitude (not used in this implementation).
     * @return The file where the recording is saved.
     * @throws IllegalStateException if starting the recording fails.
     */
    override fun startAudioRecording(
        recordingName: String?,
        amplitudePollingInterval: Long,
    ) {
        if (mediaRecorder == null) {
            initialMediaRecorderForAudio(
                saveFile = File.createTempFile(
                    "temp_recording",
                    null,
                    context.cacheDir
                )
            )
        }
        runCatching {
            // Create or get the file to save the recording
            FileHelper.createFileInCache(
                context = context,
                fileName = recordingName ?: "audio_recording_${System.currentTimeMillis()}.aac",
            )?.let { it ->

                recordingFile = it

                initialMediaRecorderForAudio(recordingFile!!)

                try {
                    mediaRecorder?.start()
                } catch (e: Exception) {
                    Log.e(TAG, "startAudioRecording: Error starting MediaRecorder: ${e.message}", e)
                    throw e
                }

                onRecordStartedListener?.onRecordStarted()

                mediaRecorderState = MediaRecorderState.RECORDING

                trackingPillingData(amplitudePollingInterval)

                return
            }
        }.onFailure {
            release()
            Log.e(TAG, "startAudioRecording: Failed to start audio recording: ${it.message}", it)
            throw it
        }.onSuccess {
            Log.d(TAG, "startAudioRecording: Audio recording started successfully.")
        }
        throw IllegalStateException("Unreachable code reached in startAudioRecording.")
    }

    /**
     * Stops the current audio recording and returns the result.
     * @return RecordResult containing details about the recording.
     * @throws IllegalStateException if stopping the recording fails.
     */
    override fun stopRecording(): RecordResult {
        if (mediaRecorder == null) {
            return RecordResult(
                success = false,
                errorMessage = "MediaRecorder is not initialized."
            )
        }

        return runCatching {
            mediaRecorder?.stop()

            // region == Calculate duration ==
            val calculateAudioDuration = recordingStartTime?.let {
                System.currentTimeMillis() - it
            }
            val audioDuration = getAudioDuration(recordingFile)
            Log.d(
                TAG,
                "stopRecording: Calculated audio duration: $calculateAudioDuration ms, Actual audio duration: $audioDuration ms"
            )

            val duration = calculateAudioDuration.takeIf { it != null && it > 0 } ?: audioDuration
            // endregion

            release()

            val recordPath = FileHelper.saveFileToMediaStore(
                context,
                recordingFile,
                MediaType.AUDIO
            )


            val result = RecordResult(
                success = true,
                filePath = recordPath,
                mimeType = audioConfig.audiEncoder.getMimType(),
                extraData = mapOf(
                    "duration_ms" to duration,
                    "bit_rate" to audioConfig.encodeBitRate,
                    "sampling_rate" to audioConfig.audiSamplingRate,
                    "channels" to audioConfig.audiChannel,
                ),
                fileName = recordingFile?.name,
                duration = duration
            )

            onRecordStoppedListener?.onRecordStopped(result)

            Log.d(
                TAG,
                "stopRecording: Audio recording stopped successfully. File saved at: $recordPath"
            )


            result
        }.getOrElse { err ->
            Log.e(TAG, "stopRecording: Failed to stop audio recording: ${err.message}", err)
            release()
            RecordResult(
                success = false,
                errorMessage = "Failed to stop recording: ${err.message}"
            )
        }
    }

    /**
     * Delete the specified recording file.
     */
    override fun deleteRecording(recordingFile: File) {
        runCatching {
            if (recordingFile.exists()) {
                recordingFile.delete()
                Log.d(TAG, "deleteRecording: Recording file deleted: ${recordingFile.absolutePath}")
            } else {
                Log.d(
                    TAG,
                    "deleteRecording: Recording file does not exist: ${recordingFile.absolutePath}"
                )
            }
        }.onFailure { err ->
            Log.e(TAG, "deleteRecording: Failed to delete recording file: ${err.message}", err)
        }
    }

    /**
     * Releases resources associated with the MediaRecorder.
     */
    override fun release() {
        mediaRecorder?.release()
        mediaRecorderState = MediaRecorderState.IDLE
    }

    override fun setOnErrorListener(listener: AppMediaRecorder.OnErrorListener) {
        mediaRecorder?.setOnErrorListener { _, what, extra ->
            listener.onError(
                appMediaRecorder = this,
                what = what,
                extra = extra
            )
        }
    }

    /**
     * Tracks the maximum duration of the current recording and notifies the listener of duration changes.
     */
    private fun trackingMaxDuration() {
        trackingRecordingDurationJob?.cancel()
        trackingRecordingDurationJob = null

        trackingRecordingDurationJob = recordCoroutine.launch {
            while (mediaRecorderState == MediaRecorderState.RECORDING) {
                val currentDuration = recordingStartTime?.let {
                    System.currentTimeMillis() - it
                } ?: 0L

                onCurrentRecordDurationChangeListener?.onCurrentRecordDurationChange(
                    appMediaRecorder = this@AudioMediaRecorder,
                    currentDuration = currentDuration
                )

                delay(1000L)
            }
        }
    }

    /**
     * Tracks the amplitude data of the recording at specified intervals.
     * @param amplitudePollingInterval The interval in milliseconds for polling amplitude data.
     */
    private fun trackingPillingData(amplitudePollingInterval: Long) {
        trackingPillingJob?.cancel()
        trackingPillingJob = null

        trackingPillingJob = recordCoroutine.launch {
            try {
                while (mediaRecorderState == MediaRecorderState.RECORDING) {
                    val maxAmplitude = mediaRecorder?.maxAmplitude

                    maxAmplitude ?: continue

                    val db = 20 * log10(maxAmplitude.toDouble())
                    val normalized = maxAmplitude / Short.MAX_VALUE.toFloat()
                    Log.d(
                        TAG,
                        "trackingPillingData: Max Amplitude: $maxAmplitude, dB: $db, Normalized: $normalized"
                    )
                    pollingData.add(normalized)

                    onPollAmplitudeListener?.onPollAmplitude(normalized)

                    delay(amplitudePollingInterval)
                }

            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "trackingPillingData: Error while polling amplitude data: ${e.message}",
                    e
                )
            }
        }
    }

    /**
     * Gets the duration of the audio file in milliseconds.
     * @param file The audio file.
     * return Duration in milliseconds, or 0 if the file is null or an error occurs.
     */
    private fun getAudioDuration(file: File?): Long {
        file ?: return 0
        // Use MediaMetadataRetriever to get the duration of the audio file
        val mediaRetriever: MediaMetadataRetriever = MediaMetadataRetriever()
        try {
            mediaRetriever.setDataSource(file.toURI().toString())
            val durationStr =
                mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

            val duration = durationStr?.toLongOrNull() ?: 0L
            try {
                mediaRetriever.release()
            } catch (_: Throwable) {
            }
            return duration
        } catch (e: Exception) {
            Log.e(TAG, "getAudioDuration: Failed to set data source: ${e.message}", e)
            return 0
        }
    }

    override fun setOnRecordStartedListener(listener: AppMediaRecorder.OnRecordStarted) {
        this.onRecordStartedListener = listener
    }

    override fun setOnRecordStoppedListener(listener: AppMediaRecorder.OnRecordStopped) {
        this.onRecordStoppedListener = listener
    }

    override fun setOnCurrentRecordDurationChangeListener(listener: AppMediaRecorder.OnCurrentRecordDurationChange) {
        this.onCurrentRecordDurationChangeListener = listener
    }

    override fun setOnMediaRecorderStateChangeListener(listener: AppMediaRecorder.OnMediaRecorderStateChange) {
        this.onMediaRecorderStateChangeListener = listener
    }

    override fun setOnPollAmplitudeListener(listener: AppMediaRecorder.OnPollAmplitudeListener) {
        this.onPollAmplitudeListener = listener
    }

}