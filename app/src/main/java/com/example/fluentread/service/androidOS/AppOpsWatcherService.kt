package com.example.fluentread.service.androidOS

import android.app.AppOpsManager
import android.app.AsyncNotedAppOp
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.SyncNotedAppOp
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.fluentread.R

@RequiresApi(Build.VERSION_CODES.R)
class AppOpsWatcherService: Service() {

    companion object {
        const val TAG = "AppOpsWatcherService"
    }

    private  lateinit var appOpsWatcher: AppOpsManager

    override fun onBind(intent: Intent?): IBinder? = null

    private  val callBack = object : AppOpsManager.OnOpNotedCallback() {
        override fun onAsyncNoted(asyncOp: AsyncNotedAppOp) {
            handleAppOp(asyncOp.op, asyncOp.message)
        }

        override fun onNoted(op: SyncNotedAppOp) {
            handleAppOp(op.op, op.attributionTag ?: "Unknown")
        }

        override fun onSelfNoted(op: SyncNotedAppOp) {
            handleAppOp(op.op, op.attributionTag ?: "Unknown")
        }
    }

    override fun onCreate() {
        super.onCreate()

        appOpsWatcher = getSystemService(AppOpsManager::class.java)
        registerAppOpsCallBack()
        startForegroundService()
    }

    // -----------------------------
    // 🌟 2. App Ops Callback
    // -----------------------------
    private fun registerAppOpsCallBack() {
        try {
            appOpsWatcher.setOnOpNotedCallback(
                mainExecutor,
                callBack
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register App Ops callback: ${e.message}")
        } catch (e: Error) {
            Log.e(TAG, "Error registering App Ops callback: ${e.message}")
        }
    }

    // -----------------------------
    // 🌟 Unregister App Ops Callback
    // -----------------------------
    private fun unregisterAppOpsCallBack() {
        try {
            appOpsWatcher.setOnOpNotedCallback(null, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister App Ops callback: ${e.message}")
        } catch (e: Error) {
            Log.e(TAG, "Error unregistering App Ops callback: ${e.message}")
        }
    }

    private fun handleAppOp(opName: String, packageOpTag: String) {
        Log.d(TAG, "App Operation Noted: $opName from package: $packageOpTag")
        when (opName) {
            AppOpsManager.OPSTR_CAMERA -> {
                // Handle camera access
            }
            AppOpsManager.OPSTR_RECORD_AUDIO -> {
                // Handle microphone access
            }
            AppOpsManager.OPSTR_FINE_LOCATION,
            AppOpsManager.OPSTR_COARSE_LOCATION -> {
                // Handle location access
            }
        }
    }

    // -----------------------------
    // 🌟 3. Foreground Notification
    // -----------------------------
    private fun startForegroundService() {
        val channelId = "privacy_monitor_service"
        val channel = NotificationChannel(
            channelId,
            "Privacy Monitor",
            NotificationManager.IMPORTANCE_MIN
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Privacy Monitor Active")
            .setContentText("Monitoring Camera, Mic, Location in real time")
            .setSmallIcon(R.drawable.app_icon)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterAppOpsCallBack()
    }

}