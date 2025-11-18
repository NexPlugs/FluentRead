package com.example.fluentread.service.media.audio

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Binder
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.compose.ui.platform.ComposeView
import com.example.fluentread.service.audio.compose.AudioRecordCompose
import com.example.fluentread.service.audio.compose.AudioViewModel
import com.example.fluentread.service.audio.models.AudioConfig
import com.example.fluentread.service.audio.models.RecordResult
import com.example.fluentread.service.audio.record.AudioMediaRecorder
import com.example.fluentread.service.notification.NotificationHelper
import com.example.fluentread.service.overlay.ToggleView
import com.example.fluentread.utils.parcelable
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Service to handle audio recording in the background
 * This use for show overlay audio recording controls and record audio or recording audio when service run foreground
 * */
@AndroidEntryPoint
class AudioRecordService: Service() {

    companion object {
        const val TAG = "AudioRecordService"

        const val ACTION_CREATE = "com.example.fluentread.service.audio.record.ACTION_CREATE"
        const val ACTION_START = "com.example.fluentread.service.audio.record.ACTION_START"
        const val ACTION_STOP = "com.example.fluentread.service.audio.record.ACTION_STOP"


        const val NOTIFICATION_ID = 0x3001
        const val NOTIFICATION_CHANNEL_ID = "AudioRecordServiceChannel"
        const val NOTIFICATION_CHANNEL_NAME = "Audio Recording Service"
    }


    /// region === Toggle valuable ===
    private var audioRecordToggle: ToggleView? = null
    @Inject lateinit var audioViewModel: AudioViewModel
    /// endregion


    /** Audio recorder instance */
    private var audioRecorder: AudioMediaRecorder? = AudioMediaRecorder.getInstance()

    private var audioConfig: AudioConfig = AudioConfig()
    private val autoRecord: Boolean
        get() = audioConfig.autoRecord

    private var binder: LocalBinder = LocalBinder()
    private var localBroadcastReceiver: LocalBroadcastReceiver = LocalBroadcastReceiver()

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()
        registerBroadcastReceiver()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null){
            when(intent.action)  {
                ACTION_CREATE -> {
                    audioConfig = intent.parcelable(AudioMediaRecorder.AUDIO_CONFIG)
                        ?: AudioConfig()
                    if(audioRecorder == null) {
                        audioRecorder = AudioMediaRecorder(
                            context = this.applicationContext,
                            audioConfig = audioConfig
                        ).apply {
                            setOnPollAmplitudeListener { amplitude -> onAmplitudeChange(amplitude) }
                            setOnRecordStoppedListener { recordResult -> onRecordingStopped(recordResult) }
                            setOnRecordStartedListener { onRecordingStarted() }
                        }
                    }

                    startNotificationForeground()

                    if(autoRecord) {
                        audioRecorder?.startAudioRecording()
                        audioViewModel.setIsRecording(true)
                    }
                }
                ACTION_START -> {
                    audioRecorder?.startAudioRecording()
                }
                ACTION_STOP -> {
                    audioRecorder?.stopRecording()
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /// region === Toggle init method function ===
    private fun createAndShowAudioRecordToggle() {
        if(audioRecordToggle != null) return
        if(!Settings.canDrawOverlays(this.applicationContext)) return
        audioRecordToggle = ToggleView(context = this.applicationContext, startPoint = Point(0, 700)).apply {
            rootGroup?.addView(ComposeView(context).apply {
                setContent {
                    AudioRecordCompose(audioRecordViewModel = audioViewModel)
                }
            })
        }

        audioRecordToggle?.show()
    }

    private fun startNotificationForeground() {
        val notificationHelper = NotificationHelper(
            context = this,
            channelId = NOTIFICATION_CHANNEL_ID,
            channelName = NOTIFICATION_CHANNEL_NAME
        )
        val builder = notificationHelper.initNotificationBuilder()
        notificationHelper.createNotificationChannel()
        startForeground(NOTIFICATION_ID, builder.build())

        createAndShowAudioRecordToggle()
    }


    /// endregion

    /// region === Register Broadcast Receiver ===
    private fun registerBroadcastReceiver() {
        try {
            val filter = android.content.IntentFilter().apply {
                addAction(Intent.ACTION_SHUTDOWN)
            }
            registerReceiver(localBroadcastReceiver, filter)

        } catch (e: Exception) {
            Log.e(TAG, "registerBroadcastReceiver: Error registering broadcast receiver", e)
        } catch (e: Error) {
            Log.e(TAG, "registerBroadcastReceiver: Error registering broadcast receiver", e)
        }
    }

    private fun unRegisterBroadcastReceiver() {
        try {
            unregisterReceiver(localBroadcastReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "UnRegisterBroadcastReceiver: Error unregistering broadcast receiver", e)
        } catch (e: Error) {
            Log.e(TAG, "UnRegisterBroadcastReceiver: Error unregistering broadcast receiver", e)
        }
    }
    /// endregion

    // Binder class to bind the service
    inner class LocalBinder : Binder() {
        fun getService(): AudioRecordService = this@AudioRecordService
    }

    // Receiver message from Device
    private inner class LocalBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            when(intent.action) {
                Intent.ACTION_SHUTDOWN -> {
                    Log.d(TAG, "onReceive: ACTION_SHUTDOWN received, stopping audio recording")
                    audioRecorder?.stopRecording()
                }
            }
        }
    }

    /// region === All recorder listener method ===
    fun onAmplitudeChange(amplitude: Float) {
        audioViewModel.setAmplitudeTracking(amplitude)
    }

    fun onRecordingStarted() {
        Log.d(TAG, "onRecordingStarted: Audio recording started")
    }

    fun onRecordingStopped(recordResult: RecordResult?) {
        //TODO: Handle recording stopped event if needed
    }
    /// endregion

    override fun onDestroy() {
        super.onDestroy()

        unRegisterBroadcastReceiver()
    }
}
