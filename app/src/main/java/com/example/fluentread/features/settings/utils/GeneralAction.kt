package com.example.fluentread.features.settings.utils

enum class GeneralAction {
    NOTIFICATION,
    PRIVACY,
    ABOUT;

    fun getTitle(): String {
        return when (this) {
            NOTIFICATION -> "Notifications"
            PRIVACY -> "Privacy Policy"
            ABOUT -> "About"
        }
    }
}