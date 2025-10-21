package com.example.fluentread.di

import android.content.Context
import androidx.room.Room
import com.example.fluentread.cache.AppDatabase
import com.example.fluentread.cache.DatabaseSettingRepository
import com.example.fluentread.cache.SettingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface  DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = AppDatabase.DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideSettingDao(appDatabase: AppDatabase) = appDatabase.settingDao()


    @Provides
    @Singleton
    fun dataBaseSettingRepository(
        settingDao: SettingDao,
        @IOScope ioScope: CoroutineScope
    ): DatabaseSettingRepository = DatabaseSettingRepository(settingDao, ioScope)
}
