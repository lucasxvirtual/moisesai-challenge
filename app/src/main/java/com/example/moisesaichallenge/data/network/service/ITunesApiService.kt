package com.example.moisesaichallenge.data.network.service

import com.example.moisesaichallenge.data.network.model.ITunesSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApiService {

    @GET("search")
    suspend fun searchMusic(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ITunesSearchResponseDto
}
