package com.example.fluentread.utils

import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Parcelable
import androidx.core.content.ContextCompat
import com.example.fluentread.service.AppController
import com.example.fluentread.service.screenRecord.ScreenRecordActivity
import com.example.fluentread.service.screenRecord.ScreenRecorder
import com.example.fluentread.service.screenRecord.models.ScreenRecordConfig


/** Launch the app controller as a foreground service */
fun Context.launchAppController() {
    val intent = Intent(this, AppController::class.java)
    ContextCompat.startForegroundService(this, intent)
}

/** Launch the screen recorder activity to start recording */
fun Context.launchScreenRecorder(screenRecordConfig: ScreenRecordConfig) {
    val startIntent = Intent(this, ScreenRecordActivity::class.java).apply {
        action = ScreenRecordActivity.ACTION_START
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        this.putExtra(ScreenRecorder.SCREEN_RECORD_CONFIG, screenRecordConfig)
    }
    startActivity(startIntent)
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}