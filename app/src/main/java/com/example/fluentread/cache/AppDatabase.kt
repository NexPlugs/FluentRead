package com.example.fluentread.cache

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Setting::class],
    version = 1,
    exportSchema = false
)
internal abstract class AppDatabase: RoomDatabase() {
    abstract fun settingDao(): SettingDao

    companion object {
        const val DATABASE_NAME = "fluentread_app_database"
    }
}