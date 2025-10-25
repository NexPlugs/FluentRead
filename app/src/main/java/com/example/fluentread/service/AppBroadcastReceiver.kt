package com.example.fluentread.service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AppBroadcastReceiver(
    private val onReceiveAction: (String) -> Unit
): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val data = intent?.getStringExtra("data") ?: return
        onReceiveAction(data)
    }
}