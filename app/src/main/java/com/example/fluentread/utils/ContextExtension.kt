package com.example.fluentread.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.fluentread.service.AppController
import com.example.fluentread.service.screenRecord.ScreenRecorder


fun Context.launchAppController() {
//    val appPermissions = AppPermissions.getInstance()
    //Implement check app permissions here

    // Implementation goes here
    val intent = Intent(this, AppController::class.java)
    ContextCompat.startForegroundService(this, intent)
}

fun Context.launchScreenRecorder() {
    val intent = Intent(this, ScreenRecorder::class.java)
    ContextCompat.startForegroundService(this, intent)
}