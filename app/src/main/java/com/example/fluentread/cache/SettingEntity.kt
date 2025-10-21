package com.example.fluentread.cache

import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity
data class Setting(
    // Unique key for the setting
    val key: String,
    // Current value of the setting
    val distanceDuration: Double,
    val lastUpdated: Long = System.currentTimeMillis(),
    val delayScroll: Double,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
