package com.example.fluentread.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class ScrollAccessibilityService: AccessibilityService() {
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {}

    override fun onInterrupt() {
        TODO("Not yet implemented") }
}