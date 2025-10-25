package com.example.fluentread.di

import com.example.fluentread.service.AppControllerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}