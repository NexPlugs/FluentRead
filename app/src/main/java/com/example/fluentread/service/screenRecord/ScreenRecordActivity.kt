package com.example.fluentread.service.screenRecord

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.fluentread.service.file.FileHelper
import com.example.fluentread.service.screenRecord.models.ScreenRecordConfig
import com.example.fluentread.utils.parcelable

/**
 * Activity to handle screen recording.
 * Currently, it does not implement any specific functionality.
 */
class ScreenRecordActivity: AppCompatActivity() {

    companion object {
        const val TAG = "RecordingActivity"

        const val ACTION_START = "com.example.fluentread.service.scrxeenRecord.action.START"
        const val ACTION_STOP = "com.example.fluentread.service.screenRecord.action.STOP"
        const val ACTION_CANCEL = "com.example.fluentread.service.screenRecord.action.CANCEL"
    }

    private var screenRecordConfig: ScreenRecordConfig? = ScreenRecordConfig()

    // MediaProjectionManager to handle screen capture intents
    private lateinit var mediaProjectionManager: MediaProjectionManager

    private val localServiceConnection: LocalServiceConnection = LocalServiceConnection()

    // Screen launcher
    private lateinit var screenCaptureLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    /** Bound service instance */
    private lateinit var boundService: ScreenRecorder
    private var isServiceBound: Boolean = false
    private var pendingAction: String? = null
    private var dataIntent: Intent? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: RecordingActivity created")

        pendingAction = intent?.action
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        screenRecordConfig = intent?.parcelable<ScreenRecordConfig>(key = ScreenRecorder.SCREEN_RECORD_CONFIG) ?: ScreenRecordConfig()

        screenCaptureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            onScreenCaptureLauncherResult(it)
        }

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            onMultiPermissionResult(it)
        }
    }

    // Handles the result from the screen capture intent.
    private fun onScreenCaptureLauncherResult(activityResult: ActivityResult) {
        Log.d(TAG, "onActivityResult: Screen capture intent result received with resultCode: ${activityResult.resultCode}")
        if(activityResult.resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: Result OK, starting screen recording")
            dataIntent = activityResult.data ?: return
            val startIntent = Intent(this, ScreenRecorder::class.java).apply {
                action = ACTION_START
                putExtra(Intent.EXTRA_INTENT, dataIntent)
                putExtra(ScreenRecorder.SCREEN_RECORD_CONFIG, screenRecordConfig)
            }
            startService(startIntent)

            this.finish()
        } else {
            FileHelper.logVideoFileInfo(this.applicationContext)
        }
    }

    // Handles the result of multiple permission requests.
    private fun onMultiPermissionResult(grantResults: Map<String, Boolean>) {
        val allGranted = grantResults.values.all { it }
        Log.d(TAG, "onMultiPermissionResult: Permissions granted? $allGranted")
        if(allGranted) {
            createScreenCaptureIntent()
        } else {
            Log.w(TAG, "onMultiPermissionResult: Permissions denied → finish()")
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, ScreenRecorder::class.java)
        startService(intent)

        bindService(intent, localServiceConnection, BIND_AUTO_CREATE)
    }

    /**
     * Starts the screen recording process.
     * Currently, this function is a placeholder and does not implement any functionality.
     * Send an intent to start or stop the ScreenRecorder service based on the action.
     */
    private fun startRecording() {
        Log.d(TAG, "startRecording: Starting recording with action: $pendingAction")
        when(pendingAction ?: "") {
            ACTION_START, "" -> { checkPermissionThenStart() }
            else -> {
                val stopIntent = Intent(this, ScreenRecorder::class.java).apply { action = ACTION_STOP }
                startService(stopIntent)
                finish()
            }
        }
    }

    // Checks and requests necessary permissions before starting screen recording.
    private fun checkPermissionThenStart() {
        val list = MediaRecordPermission.buildListScreenRecordPermissionNotGranted(this)

        if(list.isEmpty()) {
            Log.d(TAG, "checkPermissionThenStart: All permissions already granted")
            createScreenCaptureIntent()
            return
        }
        Log.d(TAG, "checkPermissionThenStart: Requesting permissions: $list")
        permissionLauncher.launch(list)

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
        Log.d(TAG, "onRequestPermissionsResult: Received permission result for requestCode: $requestCode")
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createScreenCaptureIntent()
        } else {
            // Permission denied, handle accordingly
            finish()
        }
    }

    /**
     * Inner class to handle service connection callbacks.
     */
    private inner class LocalServiceConnection : ServiceConnection {
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
        if(isServiceBound) {
            unbindService(localServiceConnection)
        }
    }

}