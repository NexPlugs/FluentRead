package com.example.fluentread.service.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.abs

/**
 * ToggleView is the floating bubble view displayed on screen use for toggling the main feature
 */
class ToggleView(
    val context: Context, startPoint: Point
): ToggleControl(context, ToggleLayout(context)) {
    /// The following variables are used to store the point (position) of the bubble
    private val prevPoint = Point(0, 0)
    private val rawPointOnDown = Point(0, 0)
    private val newPoint = Point(0, 0)

    ///The following variables are used to store the height and width of the bubble
    private var bubbleHeight = 0
    private var bubbleWidth = 0

    private var ignoreClick: Boolean = false


    init {
        // Create bubble layout config
        layoutParams?.apply {
            flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT

            gravity = Gravity.TOP or Gravity.START
            format = PixelFormat.TRANSLUCENT

            x = startPoint.x
            y = startPoint.y

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }

        customTouch()
    }

    /**
     * Update the position of the toggle bubble
     * @param x The x coordinate of the touch event
     * @param y The y coordinate of the touch event
     */
    private fun updateTogglePosition(x: Int, y: Int) {
        // Calculate the delta of the touch event
        val mIconDeltaX = x - rawPointOnDown.x
        val mIconDeltaY = y - rawPointOnDown.y

        // Update the new point of the bubble
        newPoint.x = prevPoint.x + mIconDeltaX.toInt()
        newPoint.y = prevPoint.y + mIconDeltaY.toInt()

        // Set limits to the bubble position
        val limitTop = 0
        val limitBottom = (context.resources.displayMetrics.heightPixels - bubbleHeight)

        // Apply limits to the new point
        if (newPoint.y < limitTop) {
            newPoint.y = limitTop
        } else if (newPoint.y > limitBottom) {
            newPoint.y = limitBottom
        }

        layoutParams?.x = newPoint.x
        layoutParams?.y = newPoint.y
        update()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun customTouch() {
        //Get event from behavior and update toggle position
        fun handleMovement(event: MotionEvent) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    prevPoint.x = layoutParams?.x ?: 0
                    prevPoint.y = layoutParams?.y ?: 0

                    rawPointOnDown.x = event.rawX.toInt()
                    rawPointOnDown.y = event.rawY.toInt()

                }

                MotionEvent.ACTION_MOVE -> {
                    updateTogglePosition(event.rawX.toInt(), event.rawY.toInt())
                }
            }
        }

        fun ignoreChildClickEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_UP -> ignoreClick = false
                MotionEvent.ACTION_DOWN -> ignoreClick = false
                MotionEvent.ACTION_MOVE -> {
                    if (abs(event.rawX - rawPointOnDown.x) > 1f || abs(event.rawY - rawPointOnDown.y) > 1f) {
                        ignoreClick = true
                    }
                }
            }
            return ignoreClick
        }

        if (root == null) return
        root!!.visibility =View.VISIBLE
        (root as ToggleLayout).apply {

            viewTreeObserver.addOnGlobalLayoutListener(object :
                android.view.ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Remove the listener to avoid multiple calls
                    viewTreeObserver.removeOnGlobalLayoutListener(this)

                    // Get the height and width of the bubble
                    bubbleHeight = height
                    bubbleWidth = width
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

                }
            })

            // Set the ignore child event function
            this.setOnTouchListener { _, motionEvent ->
                handleMovement(motionEvent)
                true
            }
        }
    }
}