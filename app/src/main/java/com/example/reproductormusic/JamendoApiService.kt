package com.example.reproductormusic.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Clase de datos para el Track (canción)
data class Track(
    val name: String,
    val artist_name: String, // Asegúrate de que coincida con la respuesta de Jamendo
    val image: String,       // URL de la carátula
    val audio: String        // URL del archivo de audio
) {
    // Getter simplificado para usar en el adaptador
    val artistName: String get() = artist_name
    val albumImageUrl: String get() = image
    val audioUrl: String get() = audio
}

// Clase de datos para la respuesta completa de Jamendo
data class JamendoResponse(
    val headers: Map<String, Any>,
    val results: List<Track>
)

interface JamendoApiService {

    @GET("tracks/")
    suspend fun searchTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("search") query: String, // Parámetro de búsqueda
        @Query("order") order: String = "popularity_week"
    ): Response<JamendoResponse>

    @GET("tracks/")
    suspend fun getTopCharts(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("order") order: String = "popularity_week"
    ): Response<JamendoResponse>
}