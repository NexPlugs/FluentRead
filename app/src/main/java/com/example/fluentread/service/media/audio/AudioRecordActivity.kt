package com.example.fluentread.service.media.audio

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.fluentread.service.media.audio.models.AudioConfig
import com.example.fluentread.service.media.audio.record.AudioMediaRecorder
import com.example.fluentread.service.media.utils.MediaRecordPermission
import com.example.fluentread.utils.parcelable

/**
 * Audio recording activity
 * This activity used for handling permission before start audio recording service
 */
class AudioRecordActivity: AppCompatActivity() {

    companion object {
        const val TAG = "AudioRecordActivity"

        const val ACTION_START = "com.example.fluentread.service.audio.AudioRecordActivity.START"
    }


    private var audioRecordConfig: AudioConfig = AudioConfig()

    private var pendingAction: String? = null


    /** Bound service instance */
    private val localServiceConnection: LocalServiceConnection = LocalServiceConnection()
    private lateinit var audioRecordService: AudioRecordService
    private var isServiceConnected: Boolean = false

    /** Launcher */
    private lateinit var permissionsResultLauncher: ActivityResultLauncher<Array<String>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: AudioRecordActivity created")

        pendingAction = intent?.action
        audioRecordConfig = intent.parcelable(AudioMediaRecorder.AUDIO_CONFIG) ?: AudioConfig()

        permissionsResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            onMultiPermissionResult(it)
        }
    }


    override fun onStart() {
        super.onStart()

        Log.d(TAG, "onStart: Binding audio record service")

        val intent = Intent(this, AudioRecordService::class.java)
        startService(intent)

        bindService(intent, localServiceConnection, BIND_AUTO_CREATE)
    }

    private fun checkPermissionThenLaunch() {
        val list = MediaRecordPermission.buildListAudioRecordPermissionGranted(this)

        if(list.isEmpty()) {
            Log.d(TAG, "checkPermissionThenStart: All permissions granted")
            startAndBindAudioRecordService()

            return
        }
        permissionsResultLauncher.launch(list)

    }

    // Handle multiple permission result
    private fun onMultiPermissionResult(grantResults: Map<String, Boolean>) {
        val allGranted = grantResults.values.all { it }

        if(allGranted) {
            startAndBindAudioRecordService()
        } else {
            finish()
        }
    }

    // Start and bind audio record service
    private fun startAndBindAudioRecordService() {
        Log.d(TAG, "startAndBindAudioRecordService: Starting audio record service")

        val startIntent = Intent(this, AudioRecordService::class.java).apply {
            action = AudioRecordService.ACTION_CREATE
            this.putExtra(AudioMediaRecorder.AUDIO_CONFIG, audioRecordConfig)
        }
        startService(startIntent)

        this.finish()
    }

    private fun initService() {
        when(pendingAction ?: "") {
            ACTION_START , "" -> { checkPermissionThenLaunch() }
            else -> {}
        }
    }

    // This inner class used for get ServiceInstance after bind service
    private inner class LocalServiceConnection: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected: AudioRecordService connected")
            audioRecordService = (service as AudioRecordService.LocalBinder).getService()
            isServiceConnected = true

            initService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceConnected = false
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unbindService(localServiceConnection)
    }
}