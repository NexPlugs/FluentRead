package com.example.fluentread.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.fluentread.utils.launchWithMutex
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex


/**
 * [ScrollAccessibilityService] is an AccessibilityService that can be used to scroll the screen.
 */
class ScrollAccessibilityService : AccessibilityService() {

    private var isInitializer = false

    // Mutex to ensure only one scroll action runs at a time
    private val scrollMutex = Mutex()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        const val TAG = "ScrollAccessibilityService"

        var INSTANCE: ScrollAccessibilityService? = null

        fun getInstance(): ScrollAccessibilityService? = INSTANCE
    }

    init {
        INSTANCE = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onServiceConnected() {
        Log.d(TAG, "onServiceConnected: ScrollAccessibility Service connected")
        super.onServiceConnected()
    }

    override fun onCreate() {
        super.onCreate()
        isInitializer = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Scrolls up using Mutex to prevent concurrent scrolling.
     */
    fun scrollUp() {
        serviceScope.launchWithMutex(scrollMutex) {
            performSafeScroll(isForward = false)
        }
    }

    /**
     * Scrolls down using Mutex to prevent concurrent scrolling.
     */
    fun scrollDown() {
        serviceScope.launchWithMutex(scrollMutex) {
            performSafeScroll(isForward = true)
        }
    }

    private suspend fun performSafeScroll(isForward: Boolean) = withContext(Dispatchers.Default) {
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            val success = scrollView(isForward)
            Log.d(TAG, "performSafeScroll(${if (isForward) "Down" else "Up"}): Scroll action performed: $success")
        } else {
            Log.d(TAG, "performSafeScroll: Root node is null")
        }
    }

    fun canScroll(): Boolean = true
    /**
     * Scrolls the view represented by the given [AccessibilityNodeInfo].
     */
    private fun scrollView(isForward: Boolean): Boolean {
        val root = rootInActiveWindow ?: return false
        val scrollNode = findScrollNode(root)

        val action = if (isForward)
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        else
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD

        if (scrollNode == null) {
            Log.d(TAG, "Cannot find scroll node — performing gesture fallback")
            performScrollDown(isForward)
            root.recycle()
            return false
        }

        val success = scrollNode.performAction(action)
        scrollNode.recycle()
        return success
    }

    /**
     * Performs a scroll gesture on the screen (used as fallback).
     */
    private fun performScrollDown(isForward: Boolean) {
        val displayMetrics = resources.displayMetrics
        val startX = displayMetrics.widthPixels / 2f
        val startY =
            if (isForward) displayMetrics.heightPixels * 0.8f else displayMetrics.heightPixels * 0.2f
        val endY =
            if (isForward) displayMetrics.heightPixels * 0.2f else displayMetrics.heightPixels * 0.8f

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(startX, endY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 800))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                Log.d(TAG, "Scroll gesture completed")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.w(TAG, "Scroll gesture cancelled")
            }
        }, null)
    }

    private fun logNodeTree(node: AccessibilityNodeInfo?, depth: Int = 0) {
        if (node == null) return
        val indent = " ".repeat(depth * 2)
        Log.d(
            TAG,
            "$indent Node: ${node.className}, Text: ${node.text}, ContentDescription: ${node.contentDescription}, Scrollable: ${node.isScrollable}"
        )
        for (i in 0 until node.childCount) {
            logNodeTree(node.getChild(i), depth + 1)
        }
    }

    private fun findScrollNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findScrollNode(child)
            if (found != null) return found
            child.recycle()
        }
        return null
    }

    fun autoScroll() {
        while(canScroll()) {
            scrollDown()
            Thread.sleep(2000)
        }
    }

    private fun nodeCanScroll(node: AccessibilityNodeInfo, action: Int): Boolean {
        if (node.isScrollable) {
            val actions = node.actionList.map { it.id }
            if (action in actions) return true
            val mask = node.actions
            return mask and action != 0
        }
        return false
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

}
