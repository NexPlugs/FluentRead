package com.example.fluentread.service.overlay

import android.app.Service
import android.content.Intent
import android.graphics.Point
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.fluentread.R

abstract class ToggleService: Service() {

    companion object {
        const val TAG = "ToggleService"
    }

    private var toggleView: ToggleView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if(Settings.canDrawOverlays(this).not()) {
            throw SecurityException("Overlay permission not granted")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if(Settings.canDrawOverlays(this)) {
            createToggle()
        } else {
            throw SecurityException("Overlay permission not granted")
        }
        return START_STICKY
    }

    /**
     * Create the toggle bubble
     * This function initializes the ToggleView and adds an ImageView as its content.
     */
    private fun createToggle() {
        Log.d(TAG, "createToggle: Creating toggle bubble")

        toggleView = ToggleView(context = this, startPoint = Point(0, 200))
        toggleView?.rootGroup?.addView(ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.app_icon)).apply {
                layoutParams = ViewGroup.LayoutParams(100, 100)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        toggleView = null
    }


    // Show the toggle bubble
    fun showToggle() {
        Log.d(TAG, "showToggle: Showing toggle bubble")
        toggleView?.show()
    }

}