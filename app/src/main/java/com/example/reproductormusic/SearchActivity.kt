package com.example.reproductormusic

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reproductormusic.api.ApiConfig
import com.example.reproductormusic.api.JAMENDO_CLIENT_ID
import com.example.reproductormusic.api.Track
import com.example.reproductormusic.databinding.ActivitySearchBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var trackAdapter: TrackAdapter
    private var searchJob: Job? = null
    // Retraso para no buscar en cada tecla presionada
    private val SEARCH_DELAY_MS = 800L
    // Etiqueta para filtrar el Logcat
    private val TAG = "SearchActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "1. onCreate() iniciado.")

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        setupRecyclerView()
        setupSearchInput()

        // Cargar los top charts al inicio (comportamiento de placeholder)
        searchTracks("")

        Log.d(TAG, "5. onCreate() finalizado.")
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "3. Configurando RecyclerView y Adapter.")

        // CORRECCIÓN CLAVE: Pasar el onTrackClickListener.
        // Aquí enviamos la pista seleccionada a HomeActivity para su reproducción.
        trackAdapter = TrackAdapter(emptyList()) { track ->
            // Reemplaza esta lógica con la acción deseada, como iniciar HomeActivity o un Reproductor
            Toast.makeText(this, "Reproduciendo (simulado): ${track.name}", Toast.LENGTH_SHORT).show()

            // Ejemplo de cómo podrías enviar la pista seleccionada a HomeActivity:
            // val intent = Intent(this, HomeActivity::class.java)
            // intent.putExtra("track_url", track.audioUrl)
            // startActivity(intent)
        }

        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = trackAdapter
        }
    }

    private fun setupSearchInput() {
        binding.etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Cancela el trabajo anterior de búsqueda para evitar resultados obsoletos
                searchJob?.cancel()

                val query = s.toString().trim()

                // Si está vacío, la lógica de searchTracks se encargará de cargar los charts
                if (query.isNotEmpty()) {
                    // Inicia una nueva corrutina con un delay (debounce)
                    searchJob = lifecycleScope.launch {
                        delay(SEARCH_DELAY_MS)
                        searchTracks(query)
                    }
                } else {
                    // Si el campo se vacía, carga inmediatamente los charts de nuevo
                    searchTracks("")
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        Log.d(TAG, "4. TextWatcher configurado.")
    }

    private fun searchTracks(query: String) {
        Log.d(TAG, "Iniciando búsqueda para la query: '$query'")
        if (JAMENDO_CLIENT_ID == "d5d636a2") {
            Log.w(TAG, "Advertencia: Usando clave de API de ejemplo. Reemplaza con tu clave.")
        }

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Realiza la llamada a la API
                val response = if (query.isEmpty()) {
                    // Si es vacío, carga top charts (fallback o placeholder)
                    ApiConfig.jamendoApiService.getTopCharts(JAMENDO_CLIENT_ID)
                } else {
                    // Si tiene texto, realiza la búsqueda
                    ApiConfig.jamendoApiService.searchTracks(JAMENDO_CLIENT_ID, query = query)
                }

                // LOG CRÍTICO PARA DIAGNÓSTICO
                Log.d(TAG, "Respuesta API - Código: ${response.code()}, Éxito: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    // Usar la lista de Track (Asegúrate de que Track.kt esté definido)
                    val tracks = response.body()?.results ?: emptyList<Track>()
                    trackAdapter.updateTracks(tracks)
                    Log.d(TAG, "Canciones encontradas: ${tracks.size}")

                    if (tracks.isEmpty() && query.isNotEmpty()) {
                        Toast.makeText(this@SearchActivity, "No se encontraron resultados para '$query'", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Error de código HTTP (ej. 403 Forbidden)
                    Log.e(TAG, "Fallo en la respuesta HTTP: ${response.code()} ${response.message()}")
                    Toast.makeText(this@SearchActivity, "Error de servidor: ${response.code()}. Revisa tu clave de API.", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                // Error de red (dispositivo sin internet)
                Log.e(TAG, "Error de red/IO: ${e.message}")
                Toast.makeText(this@SearchActivity, "Error de conexión. Revisa tu internet.", Toast.LENGTH_LONG).show()
            } catch (e: HttpException) {
                // Error de Retrofit/Parsing
                Log.e(TAG, "Error de HTTP/Parsing: ${e.message}", e)
                Toast.makeText(this@SearchActivity, "Error inesperado en la API.", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}