package com.example.reproductormusic.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApiService {

    // CORREGIDO: Usar JamendoResponse<Track> como tipo de retorno para la búsqueda
    @GET("tracks/")
    suspend fun searchTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("imagesize") imageSize: Int = 100,
        @Query("search") query: String // Cambiado el parámetro a 'search' para mejor claridad
    ): Response<JamendoResponse<Track>>

    // CORREGIDO: Usar JamendoResponse<Track> como tipo de retorno para los charts
    @GET("tracks/")
    suspend fun getTopCharts(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("order") order: String = "popularity_week"
    ): Response<JamendoResponse<Track>>
}