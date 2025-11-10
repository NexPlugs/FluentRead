package com.example.fluentread.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.fluentread.service.AppController
import com.example.fluentread.service.screenRecord.RecordingActivity


/** Launch the app controller as a foreground service */
fun Context.launchAppController() {
    val intent = Intent(this, AppController::class.java)
    ContextCompat.startForegroundService(this, intent)
}

/** Launch the screen recorder activity to start recording */
fun Context.launchScreenRecorder() {
    val startIntent = Intent(this, RecordingActivity::class.java).apply {
        action = RecordingActivity.ACTION_START
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(startIntent)
}