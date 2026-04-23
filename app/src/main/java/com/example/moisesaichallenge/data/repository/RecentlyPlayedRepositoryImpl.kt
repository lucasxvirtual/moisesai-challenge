package com.example.moisesaichallenge.data.repository

import com.example.moisesaichallenge.data.local.RecentlyPlayedLocalDataSource
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.RecentlyPlayedRepository
import javax.inject.Inject

class RecentlyPlayedRepositoryImpl @Inject constructor(
    private val localDataSource: RecentlyPlayedLocalDataSource
) : RecentlyPlayedRepository {

    override fun getRecentlyPlayed(): List<Track> = localDataSource.getAll()

    override fun recordPlayed(track: Track) = localDataSource.add(track)
}
