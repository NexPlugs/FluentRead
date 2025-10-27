package com.example.fluentread.service.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.example.fluentread.service.audio.models.RecordResult
import com.example.fluentread.service.file.FileHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlin.jvm.Throws

const val SAMPLING_RATE_16KHZ = 16000

const val ENCODING_BIT_RATE_32KBPS = 32000

const val MONO_CHANNEL = 1
const val STEREO_CHANNEL = 2


class AudioMediaRecorder(
    /**
     * Application context.
     */
    private val context: Context,
    /**
     * * Audio source for recording. Default is microphone.
     * MIC: use the device's microphone.
     * VOICE_COMMUNICATION: optimized for voice communication. use for video voice chat
     * VOICE_RECOGNITION: optimized for voice recognition. use for speech-to-text applications.
     * CAMCORDER: optimized for video recording. use for recording video with audio.
     * .....
     */
    private val audioSource: Int = MediaRecorder.AudioSource.MIC,

    /**
     * * Output format for the recorded audio. Default is AAC_ADTS.
     * AAC_ADTS: Advanced Audio Coding in ADTS container. -> minimal file size, good quality
     * MPEG_4: MPEG-4 Part 14 container format. -> widely supported, good quality
     * THREE_GPP: 3GPP container format. -> good for streaming, lower quality
     */
    private val outputFormat: Int = MediaRecorder.OutputFormat.AAC_ADTS,

    /**
     * * Audio encoder for the recorded audio. Default is AAC.
     * AAC: Advanced Audio Coding. -> good quality, efficient compression use for voice music
     * AMR_NB: Adaptive Multi-Rate Narrowband. -> lower quality, smaller file size -> use for call
     * AMR_WB: Adaptive Multi-Rate Wideband. -> better quality than AMR_NB, larger file size
     */
    private val audiEncoder: Int = MediaRecorder.AudioEncoder.AAC,

    /**
     * Sampling rate for the recorded audio. Default is 16kHz.
     * 8000 (small and use for call) ,
     * 11025, 16000 (good voice, safe buffer, good quality, use voice recorder , Chat voice, ..),
     * 22050, 44100 (use for CD with high quality) Hz.
     * Higher sampling rates generally result in better audio quality but larger file sizes.
     */
    private val audiSamplingRate: Int = SAMPLING_RATE_16KHZ,

    /**
     * Encoding bit per seconds to safe audio. Default is 32kbps.
     * Common bit rates for audio recording range from 16kbps to 320kbps.
     * Higher bit rates generally result in better audio quality but larger file sizes.
     */
    private val encodeBitRate: Int = ENCODING_BIT_RATE_32KBPS,

    /**
     * Number of audio channels. Default is mono channel.
     * MONO_CHANNEL: single channel audio. -> smaller file size, use for voice recording
     * STEREO_CHANNEL: two channel audio. -> better quality, use for music recording
     */
    private val audiChannel: Int = MONO_CHANNEL

): AppMediaRecorder {

    companion object {
        const val TAG = "DefaultMediaRecorder"
    }

    /**
     * Current state of the MediaRecorder.
     */
    private var mediaRecorderState: MediaRecorderState = MediaRecorderState.IDLE
        set(value) {
            field = value
            onMediaRecorderStateChangeListener?.onStateChange(value)

            when(field) {
                MediaRecorderState.RECORDING -> {
                    recordingStartTime = System.currentTimeMillis()
                    Log.d(TAG, "Recording started at $recordingStartTime")
                    // Notify listener that recording has started
                }
                else -> { recordingStartTime = 0L }
            }
        }

    /**
     * Coroutine scope for handling audio recording operations.
     */
    private val recordCoroutine: CoroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * MediaRecorder instance for audio recording.
     * The setter ensures that any existing MediaRecorder is released before assigning a new one.
     */
    private var mediaRecorder: MediaRecorder? = null
        set(value){
            if(value != null){
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

    private var onCurrentRecordDurationChangeListener: AppMediaRecorder.OnCurrentRecordDurationChange? = null

    private var onMediaRecorderStateChangeListener: AppMediaRecorder.OnMediaRecorderStateChange? = null


    // Implement all function of media recorder

    /**
     * Builds and returns a MediaRecorder instance base on android version.
     */
    fun buildMediaRecorder(): MediaRecorder {
        //If version android is S or higher, use the builder pattern to create MediaRecorder
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
            setAudioSource(audioSource)
            setOutputFormat(outputFormat)
            setAudioEncoder(audiEncoder)
            setAudioSamplingRate(audiSamplingRate)
            setAudioEncodingBitRate(encodeBitRate)
            setAudioChannels(audiChannel)
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
        recordingName: String,
        amplitudePollingInterval: Long,
    ): Unit {
        if(mediaRecorder == null) {
            initialMediaRecorderForAudio(
                saveFile = File.createTempFile("temp_recording", null, context.cacheDir)
            )
        }
        runCatching {
            // Create or get the file to save the recording
            FileHelper.createFileInCache(
                context = context,
                fileName = recordingName,
            )?.let { it ->
                recordingFile = it
                mediaRecorder?.start()
                onRecordStartedListener?.onRecordStarted()

                mediaRecorderState = MediaRecorderState.RECORDING

                initialMediaRecorderForAudio(it)

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

    override fun stopRecording(): RecordResult {
        if(mediaRecorder == null) {
            return RecordResult(
                success = false,
                errorMessage = "MediaRecorder is not initialized."
            )
        }

        return runCatching {
            mediaRecorder?.stop()
            val calcuAudioDuration = recordingStartTime?.let {
                System.currentTimeMillis() - it
            }
            val recordedFile = recordingFile
        }.getOrElse { err ->
            Log.e(TAG, "stopRecording: Failed to stop audio recording: ${err.message}", err)
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
                Log.d(TAG, "deleteRecording: Recording file does not exist: ${recordingFile.absolutePath}")
            }
        }.onFailure { err ->
            Log.e(TAG, "deleteRecording: Failed to delete recording file: ${err.message}", err)
        }
    }

    override fun release() {
        mediaRecorder?.release()
        mediaRecorderState = MediaRecorderState.IDLE
        onRecordStoppedListener?.onRecordStopped()
    }

    override fun setOnErrorListener(listener: AppMediaRecorder.OnErrorListener) {
        mediaRecorder?.setOnErrorListener {
            _, what, extra ->
            listener.onError(
                appMediaRecorder = this,
                what = what,
                extra = extra
            )
        }
    }

    private fun getAudioDuration(file: File?): Long {
        file ?: return 0
        // Use MediaMetadataRetriever to get the duration of the audio file
        val mediaRetriever: MediaMetadataRetriever = MediaMetadataRetriever()
        try {
            mediaRetriever.setDataSource(file.toURI().toString())
            val durationStr = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L
            try {
                mediaRetriever.release()
            } catch (_: Throwable) {}
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
}