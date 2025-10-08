package com.example.fluentread.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object for the Setting entity.
 * Provides methods for inserting, querying, updating, and deleting settings in the database.
 * Each method is a suspend function to support asynchronous operations with Kotlin coroutines.
 */
@SuppressWarnings("TooManyFunctions")
@Dao
internal interface SettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: Setting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<Setting>)

    @Query("SELECT * FROM Setting WHERE `key` = :key LIMIT 1")
    suspend fun getSettingByKey(key: String): Setting?

    @Query("SELECT * FROM Setting")
    suspend fun getAllSettings(): List<Setting>

    @Query("DELETE FROM Setting WHERE `key` = :key")
    suspend fun deleteSettingByKey(key: String)

    @Query("DELETE FROM Setting")
    suspend fun deleteAllSettings()


    @Query("UPDATE Setting SET value = :value, lastUpdated = :lastUpdated WHERE `key` = :key")
    suspend fun updateSettingValue(key: String, value: String, lastUpdated: Long = System.currentTimeMillis())

    @Query("SELECT * FROM Setting WHERE isVisible = 1 ORDER BY `group`, `order`")
    suspend fun getVisibleSettings(): List<Setting>

}
