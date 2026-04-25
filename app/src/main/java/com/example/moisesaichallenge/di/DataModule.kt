package com.example.moisesaichallenge.di

import com.example.moisesaichallenge.data.cache.InMemoryMusicCache
import com.example.moisesaichallenge.data.cache.MusicCache
import com.example.moisesaichallenge.data.local.InMemoryRecentlyPlayedDataSource
import com.example.moisesaichallenge.data.local.RecentlyPlayedLocalDataSource
import com.example.moisesaichallenge.data.network.datasource.AlbumRemoteDataSource
import com.example.moisesaichallenge.data.network.datasource.AlbumRemoteDataSourceImpl
import com.example.moisesaichallenge.data.network.datasource.MusicRemoteDataSource
import com.example.moisesaichallenge.data.network.datasource.MusicRemoteDataSourceImpl
import com.example.moisesaichallenge.data.repository.AlbumRepositoryImpl
import com.example.moisesaichallenge.data.repository.MusicRepositoryImpl
import com.example.moisesaichallenge.data.repository.RecentlyPlayedRepositoryImpl
import com.example.moisesaichallenge.domain.repository.AlbumRepository
import com.example.moisesaichallenge.domain.repository.MusicRepository
import com.example.moisesaichallenge.domain.repository.RecentlyPlayedRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds @Singleton
    abstract fun bindMusicRemoteDataSource(impl: MusicRemoteDataSourceImpl): MusicRemoteDataSource

    @Binds @Singleton
    abstract fun bindAlbumRemoteDataSource(impl: AlbumRemoteDataSourceImpl): AlbumRemoteDataSource

    @Binds @Singleton
    abstract fun bindMusicCache(impl: InMemoryMusicCache): MusicCache

    @Binds @Singleton
    abstract fun bindRecentlyPlayedDataSource(impl: InMemoryRecentlyPlayedDataSource): RecentlyPlayedLocalDataSource

    @Binds @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository

    @Binds @Singleton
    abstract fun bindAlbumRepository(impl: AlbumRepositoryImpl): AlbumRepository

    @Binds @Singleton
    abstract fun bindRecentlyPlayedRepository(impl: RecentlyPlayedRepositoryImpl): RecentlyPlayedRepository
}
