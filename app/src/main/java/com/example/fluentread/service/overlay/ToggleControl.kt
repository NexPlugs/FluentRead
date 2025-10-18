package com.example.fluentread.service.overlay

import android.content.Context
import android.view.ViewGroup
import android.view.WindowManager

/**
 * ToggleControl is a class that is used to control the toggle view.
 * It is used to show, remove and update the toggle view.
 * @param context Context is used to get the WindowManager service.
 * @param root ViewGroup? is used to set the root view group.
 *
 */
open class ToggleControl(context: Context, root: ViewGroup? = null) {

    // WindowManager is a class that is responsible for managing the window.
    private var _windowManager: WindowManager? = null

    // WindowManager.LayoutParams is a class that is responsible for managing the layout parameters of the window.
    private var _rootParams: WindowManager.LayoutParams? = null

    // It is used to store the window manager.
    private val windowManager: WindowManager
        get() = _windowManager ?: throw IllegalStateException("WindowManager is not initialized")

    // ComposeLifeCycleOwner is a class that is responsible for managing the lifecycle of the compose view.
    private var composeOwner: ComposeLifeCycleOwner? = null

    /// _root is a private variable of type ViewGroup. It is used to store the root view group.
    private var _root: ViewGroup? = null
    /// isComposeOwnerInitialized is a private variable of type Boolean. It is used to determine if the compose owner is initialized.
    private var isComposeOwnerInitialized: Boolean = false
    val rootGroup get() = root

    var root
        get() = _root
        set(value) {
            _root = value
        }

    var layoutParams
        get() = _rootParams
        set(value) {
            _rootParams = value
        }

    init {
        _windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        _rootParams = WindowManager.LayoutParams()
        _root = root

        if (_root != null) {
            composeOwner = ComposeLifeCycleOwner()
            composeOwner?.attachToDecorView(_root)
        }
    }


    /// show is a method that is used to show the bubble view.
    fun show(): Boolean {
        /// If the root view group is null or the window token is not null, then return false.
        if (root?.windowToken != null) return false
        try {
            ///✨ Handling if containCompose is true
            if (isComposeOwnerInitialized.not()) {
                composeOwner?.onCreate()
                isComposeOwnerInitialized = true
            }
            composeOwner?.onStart()
            composeOwner?.onResume()
            /// Adding the root view group to the window manager.
            if(root?.isAttachedToWindow == false) {
                windowManager.addView(root, _rootParams)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /// remove is a method that is used to remove the bubble view.
    fun remove(): Boolean{
        /// If the root view group is null or the window token is null, then return false.
        if(root?.windowToken == null) return false
        try {
            /// Removing the root view group from the window manager.
            windowManager.removeView(root)
            ///✨ Handling if containCompose is true
            composeOwner?.onPause()
            composeOwner?.onStop()
            composeOwner?.onDestroy()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /// update is a method that is used to update the bubble view.
    fun update() {
        /// If the root view group is null or the window token is null, then return.
        if(root?.windowToken == null) return
        try {
            /// Updating the root view group.
            windowManager.updateViewLayout(root, _rootParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}