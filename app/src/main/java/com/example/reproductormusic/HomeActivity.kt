package com.example.reproductormusic

import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.reproductormusic.api.ApiConfig
import com.example.reproductormusic.api.JAMENDO_CLIENT_ID
import com.example.reproductormusic.api.Track
import com.example.reproductormusic.databinding.ActivityHomeBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var instantSearchAdapter: TrackAdapter
    private var searchJob: Job? = null
    private val SEARCH_DELAY_MS = 800L
    private val TAG = "HomeActivitySearch"

    // --- Variables del Reproductor ---
    // ¡DECLARADO COMO 'var' para permitir reasignación!
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingTrack: Track? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInstantSearchRecyclerView()
        setupSearchInput()
        setupPlayerControls()

        binding.playerBar.visibility = View.GONE
    }

    private fun setupInstantSearchRecyclerView() {
        instantSearchAdapter = TrackAdapter(emptyList()) { track ->
            playTrack(track)
        }
        binding.rvInstantResults.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = instantSearchAdapter
        }
    }

    private fun setupPlayerControls() {
        binding.playerBar.findViewById<ImageView>(R.id.iv_player_play_pause)?.setOnClickListener {
            togglePlayback()
        }
    }

    private fun togglePlayback() {
        mediaPlayer?.let { player ->
            if (isPlaying) {
                player.pause()
                isPlaying = false
                updatePlayPauseIcon(R.drawable.ic_play_arrow)
                Toast.makeText(this, "Pausado: ${currentPlayingTrack?.name}", Toast.LENGTH_SHORT).show()
            } else if (currentPlayingTrack != null) {
                player.start()
                isPlaying = true
                updatePlayPauseIcon(R.drawable.ic_pause)
                Toast.makeText(this, "Reproduciendo: ${currentPlayingTrack?.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePlayPauseIcon(drawableResId: Int) {
        binding.playerBar.findViewById<ImageView>(R.id.iv_player_play_pause)
            ?.setImageResource(drawableResId)
    }

    private fun setHomeContentVisibility(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        binding.svHomeContent.visibility = visibility
    }

    private fun setupSearchInput() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    setHomeContentVisibility(true)
                    binding.rvInstantResults.visibility = View.GONE
                    instantSearchAdapter.updateTracks(emptyList())
                    return
                }
                setHomeContentVisibility(false)
                binding.rvInstantResults.visibility = View.VISIBLE
                if (query.length >= 3) {
                    searchJob = lifecycleScope.launch {
                        delay(SEARCH_DELAY_MS)
                        searchTracks(query)
                    }
                } else {
                    instantSearchAdapter.updateTracks(emptyList())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun playTrack(track: Track) {
        // 1. Detener y liberar el reproductor anterior
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // Se usa una variable 'val' local para la instancia de MediaPlayer,
        // esto hace más limpia la configuración de listeners.
        val newMediaPlayer = MediaPlayer()

        try {
            newMediaPlayer.setDataSource(track.audioUrl)
            newMediaPlayer.prepareAsync()

            newMediaPlayer.setOnPreparedListener { mp ->
                mp.start()
                this.isPlaying = true
                this.currentPlayingTrack = track
                Toast.makeText(
                    this@HomeActivity,
                    "Reproduciendo: ${track.name}",
                    Toast.LENGTH_SHORT
                ).show()

                updatePlayerBar(track)
                updatePlayPauseIcon(R.drawable.ic_pause)
            }

            newMediaPlayer.setOnErrorListener { _, what, extra ->
                Toast.makeText(
                    this@HomeActivity,
                    "Error al reproducir: ${track.name}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Error de MediaPlayer: $what, $extra")
                newMediaPlayer.release()
                this.mediaPlayer = null
                this.isPlaying = false
                updatePlayPauseIcon(R.drawable.ic_play_arrow)
                true
            }

            // 2. Asignación FINAL a la variable de la clase ('var').
            // Usamos 'this.' para asegurar que el compilador use la variable de la clase.
            this.mediaPlayer = newMediaPlayer

        } catch (e: Exception) {
            Log.e(TAG, "Error de setDataSource: ${e.message}")
            Toast.makeText(
                this@HomeActivity,
                "Error al cargar la fuente de audio.",
                Toast.LENGTH_LONG
            ).show()
            newMediaPlayer.release()
            this.mediaPlayer = null
        }
    }

    private fun updatePlayerBar(track: Track) {
        binding.playerBar.findViewById<TextView>(R.id.tv_player_title)?.text = track.name
        binding.playerBar.findViewById<TextView>(R.id.tv_player_artist)?.text = track.artistName

        binding.playerBar.findViewById<ImageView>(R.id.iv_player_cover)?.let { iv ->
            Glide.with(this).load(track.imageUrl).into(iv)
        }

        binding.playerBar.visibility = View.VISIBLE
    }

    private fun searchTracks(query: String) {
        lifecycleScope.launch {
            try {
                val response = ApiConfig.jamendoApiService.searchTracks(
                    clientId = JAMENDO_CLIENT_ID,
                    query = query,
                    limit = 20
                )
                if (response.isSuccessful) {
                    val tracks = response.body()?.results ?: emptyList()
                    instantSearchAdapter.updateTracks(tracks)
                } else {
                    Log.e(TAG, "Error en respuesta: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error de red o desconocido: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
