package com.example.fluentread.service.screenRecord

import android.app.ComponentCaller
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity to handle screen recording.
 * Currently, it does not implement any specific functionality.
 */
class RecordingActivity: AppCompatActivity() {

    companion object {
        const val TAG = "RecordingActivity"

        const val ACTION_START = "com.example.fluentread.service.screenRecord.action.START"
        const val ACTION_STOP = "com.example.fluentread.service.screenRecord.action.STOP"
        const val  ACTION_CANCEL = "com.example.fluentread.service.screenRecord.action.CANCEL"
    }

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var action: String? = null
    private val localServiceConnection: LocalServiceConnection = LocalServiceConnection()
    private lateinit var screenCaptureLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        action = intent?.action

        screenCaptureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {  }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, ScreenRecorder::class.java)
        startService(intent)
        bindService(intent, localServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)
    }

    /**
     * Starts the screen recording process.
     * Currently, this function is a placeholder and does not implement any functionality.
     */
    private fun startRecording() {}

    /**
     * Checks for necessary permissions.
     * Currently, this function is a placeholder and does not implement any functionality.
     */
    private fun checkPermissionThenStart() {}

    /**
     * Inner class to handle service connection callbacks.
     */
    private inner class LocalServiceConnection : ServiceConnection{
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            startRecording()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            TODO("Not yet implemented")
        }
    }

    /**
     * Creates an intent to capture the screen and launches it.
     */
    private fun createScreenCaptureIntent() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }

}