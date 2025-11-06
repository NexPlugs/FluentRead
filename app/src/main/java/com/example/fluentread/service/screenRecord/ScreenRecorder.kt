package com.example.fluentread.service.screenRecord

import android.app.Service
import android.content.Intent
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import com.example.fluentread.service.screenRecord.models.ScreenRecordState
import java.io.File

/**
 * Service responsible for screen recording functionality.
 */
object ScreenRecorder: Service(), AppScreenRecorder {

    const val TAG = "ScreenRecorder"

    private var outPutFile: File? = null

    private var mediaProjection: MediaProjection? = null

    private var mediaProjectionManager: MediaProjectionManager? = null

    private var virtualDisplay: VirtualDisplay? = null

    private var screenRecordState: ScreenRecordState = ScreenRecordState.IDLE
        set(value)  {
            field = value
            //TODO: Notify listeners of state change
        }

    private var onErrorListener: AppScreenRecorder.OnErrorListener? = null


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        // TODO: Implement screen recordinginitialization
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO: Implement screen recording cleanup
    }

    override fun startRecording() {
        TODO("Not yet implemented")
    }

    override fun stopRecording() {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun resume() {
        TODO("Not yet implemented")
    }

    override fun isRecording(): Boolean {
        TODO("Not yet implemented")
    }
}