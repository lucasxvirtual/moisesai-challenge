package com.example.moisesaichallenge.di

import com.example.moisesaichallenge.data.network.client.ApiClientProvider
import com.example.moisesaichallenge.data.network.service.ITunesApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideITunesApiService(): ITunesApiService =
        ApiClientProvider.createService(ITunesApiService::class.java)
}
