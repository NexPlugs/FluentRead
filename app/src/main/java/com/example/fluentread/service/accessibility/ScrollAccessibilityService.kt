package com.example.fluentread.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * [ScrollAccessibilityService] is an AccessibilityService that can be used to scroll the screen.
 */
class ScrollAccessibilityService : AccessibilityService(), LifecycleOwner {

    // A flag to check if the service is initialized
    private var isInitializer = false

    // Lifecycle registry to manage the lifecycle of the service
    private lateinit var lifeCycleRegistry: LifecycleRegistry


    companion object {
        const val TAG = "ScrollAccessibilityService"
    }

    // Implementing LifecycleOwner to manage the lifecycle of the service
    override val lifecycle: Lifecycle
        get() = lifeCycleRegistry


    //[onAccessibilityEvent] is called when an accessibility event is fired.
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "onAccessibilityEvent: ${event?.eventType}")
    }

    //[onInterrupt] is called when the service is interrupted.
    override fun onInterrupt() {
        TODO("Not yet implemented") }

    override fun onServiceConnected() {
        Log.d(TAG, "onServiceConnected: ScrollAccessibility Service connected")
        this.serviceInfo = this.serviceInfo.apply {
            // Specify the types of events to listen for here
            eventTypes = AccessibilityEvent.TYPE_VIEW_SCROLLED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        }
        super.onServiceConnected( )
    }

// [onCreate] is called when the service is created.
    override fun onCreate() {
        super.onCreate()

        try {
            lifeCycleRegistry = LifecycleRegistry(this)
            lifeCycleRegistry.currentState = Lifecycle.State.CREATED
            isInitializer = true
        } catch (e: Exception) {
            Log.d(TAG, "ScrollAccessibility Service onCreate: ${e.message}")
            isInitializer = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    // Define scrolling methods here
    fun scrollUp() {
        // Implement scroll up logic
    }

    fun scrollDown() {
        // Implement scroll down logic
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    /**
     *  Scrolls the view represented by the given [AccessibilityNodeInfo].
     */
    private fun scrollView(node: AccessibilityNodeInfo?): Boolean {
        if(node == null) return false

        if(node.isScrollable) {
            return false
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    /**
     * Logs the tree structure of the given [AccessibilityNodeInfo] for debugging purposes.
     * @param node The root node to start logging from.
     * @param depth The current depth in the tree, used for indentation (default is 0).
     *
     */
    private fun logNodeTree(node: AccessibilityNodeInfo?, depth: Int = 0) {
        if (node == null) return

        val indent = " ".repeat(depth * 2)
        Log.d(TAG, "$indent Node: ${node.className}, Text: ${node.text}, ContentDescription: ${node.contentDescription}, Scrollable: ${node.isScrollable}")

        for (i in 0 until node.childCount) {
            logNodeTree(node.getChild(i), depth + 1)
        }
    }

}