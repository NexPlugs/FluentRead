package com.example.fluentread.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.Flow


/**
 * [ScrollAccessibilityService] is an AccessibilityService that can be used to scroll the screen.
 */
class ScrollAccessibilityService : AccessibilityService() {

    // A flag to check if the service is initialized
    private var isInitializer = false


    companion object {
        const val TAG = "ScrollAccessibilityService"

        //Singleton instance of ScrollAccessibilityService
        var INSTANCE: ScrollAccessibilityService? = null

        fun getInstance(): ScrollAccessibilityService? {
            return INSTANCE
        }
    }

    init {
        INSTANCE = this
    }

    //[onAccessibilityEvent] is called when an accessibility event is fired.
    override fun onAccessibilityEvent(event: AccessibilityEvent?) { }

    //[onInterrupt] is called when the service is interrupted.
    override fun onInterrupt() {
        TODO("Not yet implemented") }

    override fun onServiceConnected() {
        Log.d(TAG, "onServiceConnected: ScrollAccessibility Service connected")
//        this.serviceInfo = this.serviceInfo.apply {
//            // Specify the types of events to listen for here
//            eventTypes = AccessibilityEvent.TYPE_VIEW_SCROLLED or
//                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
//        }
        super.onServiceConnected( )
    }

    // [onCreate] is called when the service is created.
    override fun onCreate() {
        super.onCreate()

        isInitializer = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    // Define scrolling methods here
    fun scrollUp() {
        // Implement scroll up logic
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            val success = scrollView(isForward = false)
            Log.d(TAG, "scrollUp: Scroll action performed: $success")
        } else {
            Log.d(TAG, "scrollUp: Root node is null")
        }
    }

    fun scrollDown() {
        // Implement scroll down logic
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            val success = scrollView(isForward = true)
            Log.d(TAG, "scrollDown: Scroll action performed: $success")
        } else {
            Log.d(TAG, "scrollDown: Root node is null")
        }
    }

    fun canScroll() : Boolean {
        //TODO: Implement can scroll logic
        return  true
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     *  Scrolls the view represented by the given [AccessibilityNodeInfo].
     */
    private fun scrollView(isForward: Boolean) {
        val root = rootInActiveWindow ?: return
        val scrollNode = findScrollNode(root)

        val action = if( isForward)
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD

        if(scrollNode == null) {
            Log.d(TAG, "Can not find scroll node")
            performScrollDown(isForward)
            root.recycle()
            return
        }

        scrollNode.performAction(action)
        scrollNode.recycle()
    }

    /**
     * Performs a scroll down gesture on the screen.
     * Using path to define the gesture from bottom to top.
     */
    private fun performScrollDown(isForward: Boolean) {
        val displayMetrics = resources.displayMetrics
        val startX = displayMetrics.widthPixels / 2f
        val startY = if(isForward) displayMetrics.heightPixels * 0.2f else displayMetrics.heightPixels * 0.8f

        val endX = startX
        val endY = if(isForward) displayMetrics.heightPixels * 0.8f else displayMetrics.heightPixels * 0.2f

        // Create a behavior action by using Path()
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300)) // 300ms swipe
            .build()

        // Sink action to device by using behavior that was created before
        dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                Log.d(TAG, "Scroll completed")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
            }
        }, null)
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

    /**
     * Use pattern to find scroll Node for perform scroll action
     * @param node the first node in tree
     */
    private fun findScrollNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if(node.isScrollable) return node
        for(i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val findScrollChild = findScrollNode(child)
            if(findScrollChild != null) return findScrollChild
            child.recycle()
        }
        return null
    }

    /**
     * Check if the node can perform the scroll action
     * @param node The AccessibilityNodeInfo to check.
     * @param action The scroll action to check (e.g., ACTION_SCROLL_FORWARD or ACTION_SCROLL_BACKWARD).
     */
    private fun nodeCanScroll(node: AccessibilityNodeInfo, action: Int): Boolean {
        if(node.isScrollable) {
            val actions = node.actionList.map { it.id }
            for( act in actions) {
                if(act == action) return true
            }
            val mask = node.actions
            return mask and action != 0
        }
        return false
    }
}