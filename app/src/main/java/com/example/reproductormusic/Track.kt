package com.example.reproductormusic.api

import com.google.gson.annotations.SerializedName

// Define tu Client ID de Jamendo aquí
const val JAMENDO_CLIENT_ID = "d5d636a2"

// Estructura general de la respuesta de Jamendo
data class JamendoResponse<T>(
    @SerializedName("headers")
    val headers: Map<String, String>,
    @SerializedName("results")
    val results: List<T>
)

// Modelo de datos para una canción (Track)
data class Track(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("artist_name")
    val artistName: String,
    @SerializedName("audio")
    val audioUrl: String, // Usado en HomeActivity para la reproducción
    @SerializedName("image")
    val imageUrl: String, // Usado en HomeActivity y Adapter para la carátula
)