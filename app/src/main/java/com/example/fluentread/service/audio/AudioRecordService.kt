package com.example.fluentread.service.audio

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.fluentread.service.audio.models.AudioConfig
import com.example.fluentread.service.audio.record.AudioMediaRecorder
import com.example.fluentread.utils.parcelable

/**
 * Service to handle audio recording in the background
 * This use for show overlay audio recording controls and record audio
 * */
class AudioRecordService: Service() {

    companion object {
        const val TAG = "AudioRecordService"

        const val ACTION_CREATE = "com.example.fluentread.service.audio.record.ACTION_CREATE"
        const val ACTION_START = "com.example.fluentread.service.audio.record.ACTION_START"
        const val ACTION_STOP = "com.example.fluentread.service.audio.record.ACTION_STOP"
    }


    /** Audio recorder instance */
    private var audioRecorder: AudioMediaRecorder? = null

    private var audioConfig: AudioConfig = AudioConfig()

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
                    audioConfig = intent.parcelable<AudioConfig>(AudioMediaRecorder.AUDIO_CONFIG) ?: AudioConfig()
                    Log.d(AudioMediaRecorder.Companion.TAG, "onStartCommand: Creating audio recorder with config: $audioConfig")
                    audioRecorder = AudioMediaRecorder(context = this, audioConfig = audioConfig)
                }
                ACTION_START -> {
                    audioRecorder?.startAudioRecording()
                }
                ACTION_STOP -> {
                    Log.d(AudioMediaRecorder.Companion.TAG, "onStartCommand: Stopping audio recording")
                    audioRecorder?.stopRecording()
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

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
    private inner class LocalBinder : Binder() {
        fun getService(): AudioRecordService = this@AudioRecordService
    }

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

    override fun onDestroy() {
        super.onDestroy()

        unRegisterBroadcastReceiver()
    }
}