package com.example.fluentread.service.screenRecord

import android.app.ComponentCaller
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.fluentread.permissions.AppPermissions

/**
 * Activity to handle screen recording.
 * Currently, it does not implement any specific functionality.
 */
class RecordingActivity: AppCompatActivity() {

    companion object {
        const val TAG = "RecordingActivity"

        const val ACTION_START = "com.example.fluentread.service.screenRecord.action.START"
        const val ACTION_STOP = "com.example.fluentread.service.screenRecord.action.STOP"
        const val ACTION_CANCEL = "com.example.fluentread.service.screenRecord.action.CANCEL"
    }

    private lateinit var mediaProjectionManager: MediaProjectionManager

    private val localServiceConnection: LocalServiceConnection = LocalServiceConnection()
    private lateinit var screenCaptureLauncher: ActivityResultLauncher<Intent>

    /** Bound service instance */
    private lateinit var boundService: ScreenRecorder
    private var isServiceBound: Boolean = false
    private var action: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: RecordingActivity created")

        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        action = intent?.action
        screenCaptureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            mediaProjectionManager.createScreenCaptureIntent()
        }
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

        Log.d(TAG, "onActivityResult: Received activity result with requestCode: $requestCode, resultCode: $resultCode")
        if (requestCode == ScreenRecorder.REQUEST_CODE_SCREEN_CAPTURE) {
            // TODO: Handle screen capture result
        }
    }

    /**
     * Starts the screen recording process.
     * Currently, this function is a placeholder and does not implement any functionality.
     * Send an intent to start or stop the ScreenRecorder service based on the action.
     */
    private fun startRecording() {
        Log.d(TAG, "startRecording: Starting recording with action: $action")
        when(action ?: "") {
            ACTION_START, "" -> { checkPermissionThenStart() }
            else -> {
                val stopIntent = Intent(this, ScreenRecorder::class.java).apply { action = ACTION_STOP }
                startService(stopIntent)
                finish()
            }
        }
    }

    /**
     * Checks for necessary permissions.
     * Currently, this function is a placeholder and does not implement any functionality.
     */
    private fun checkPermissionThenStart() {
        Log.d(TAG, "checkPermissionThenStart: Checking permissions")
        if(!AppPermissions.getInstance().isWriteExternalStoragePermissionGranted(this)) {
            Log.d(TAG, "checkPermissionThenStart: Requesting WRITE_EXTERNAL_STORAGE permission")
            val permissions = arrayListOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            //TODO: Check if include audio
            permissions.plus(android.Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), ScreenRecorder.REQUEST_CODE_PERMISSION)
        } else {
            createScreenCaptureIntent()
        }
    }

    /** Handles the result of permission requests.
     * If permissions are granted, it initiates the screen capture intent.
     * If permissions are denied, it finishes the activity.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if(grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            createScreenCaptureIntent()
        } else {
            // Permission denied, handle accordingly
            finish()
        }
    }

    /**
     * Inner class to handle service connection callbacks.
     */
    private inner class LocalServiceConnection : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            boundService = (service as ScreenRecorder.LocalBinder).getService()
            isServiceBound = true

            startRecording()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

    /**
     * Creates an intent to capture the screen and launches it.
     */
    private fun createScreenCaptureIntent() {
        Log.d(TAG, "createScreenCaptureIntent: Creating screen capture intent")
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }

    override fun onStop() {
        super.onStop()
        unbindService(localServiceConnection)
    }

}