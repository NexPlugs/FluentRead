package com.example.fluentread.di

import android.content.Context
import com.example.fluentread.service.AppControllerRepository
import com.example.fluentread.service.audio.record.AudioMediaRecorder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {
    @Provides
    @IOScope
    fun provideIOScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    @Singleton
    @Provides
    fun provideAppControllerRepository(): AppControllerRepository {
        return AppControllerRepository()
    }


    @Provides
    @Singleton
    fun provideAudiRecorder(
        @ApplicationContext context: Context
    ): AudioMediaRecorder {
        return AudioMediaRecorder(context = context)
    }
}