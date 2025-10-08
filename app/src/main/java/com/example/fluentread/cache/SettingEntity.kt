package com.example.fluentread.cache

import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity
data class Setting(
    // Unique key for the setting
    val key: String,
    // Current value of the setting
    val value: String,
    // Metadata
    val type: String,
    val description: String,
    val group: String,
    val order: Int,
    val options: String? = null,
    val isVisible: Boolean = true,
    val isEditable: Boolean = true,
    val isAdvanced: Boolean = false,
    val isDeprecated: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)