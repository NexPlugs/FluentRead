package com.example.fluentread.cache

import androidx.collection.LruCache
import com.example.fluentread.utils.launchWithMutex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

/**
 * Repository for managing settings in the database with caching.
 */
@SuppressWarnings("TooManyFunctions")
class DatabaseSettingRepository(
    // The Data Access Object for settings
    private val settingDao: SettingDao,
    // Coroutine scope for asynchronous operations
    private val coroutineScope: CoroutineScope,
    // Size of the in-memory cache
    cacheSize: Int = 100
) {
    private val settingCache = LruCache<Int, Setting>(cacheSize)
    private val mutex = Mutex()

    /**
     * Retrieves the first setting from the cache or database.
     * @return The first Setting object or null if none exist.
     */
    fun getSetting(): Setting? {
        return settingCache.snapshot().values.firstOrNull() ?: run {
            var setting: Setting? = null
            coroutineScope.launchWithMutex(mutex) {
                setting = settingDao.getAllSettings().firstOrNull()
                setting?.let {
                    settingCache.put(it.id, it)
                }
            }.invokeOnCompletion {
                // No-op
            }
            setting
        }
    }

    /**
     * Inserts or updates a single setting in the database and cache.
     * @param setting The Setting object to insert or update.
     */
    fun insertSetting(setting: Setting) {
        insertListSetting(listOf(setting))
    }

    /**
     * Inserts or updates a list of settings in the database and cache.
     * @param settings The list of Setting objects to insert or update.
     */
    fun insertListSetting(settings: List<Setting>) {
        if(settings.isEmpty()) return
        val settingToInsert = settings.filter {
            val cachedSetting = settingCache[it.id]
            cachedSetting == null || cachedSetting != it
        }
        cacheListSettings(settingToInsert)
        coroutineScope.launchWithMutex(mutex) {
            settingDao.insertAll(settingToInsert)
        }
    }

    /**
     * Caches a list of settings in the in-memory cache.
     * @param settings The list of Setting objects to cache.
     */
    private fun cacheListSettings(settings: List<Setting>) {
        settings.forEach {
            settingCache.put(it.id, it)
        }
    }

}