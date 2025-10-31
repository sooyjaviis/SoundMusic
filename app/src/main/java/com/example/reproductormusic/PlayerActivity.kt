package com.example.reproductormusic

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.reproductormusic.databinding.ActivityPlayerBinding
import java.io.IOException
import java.util.concurrent.TimeUnit

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var handler: Handler


    private var currentAudioUrl: String? = null
    private var isPlaying = false

    private val TAG = "PlayerActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        handler = Handler(Looper.getMainLooper())

        // --- 1. Obtener Datos del Intent ---
        val songTitle = intent.getStringExtra("SONG_TITLE") ?: "Título Desconocido"
        val artistName = intent.getStringExtra("ARTIST_NAME") ?: "Artista Desconocido"
        val albumArtUrl = intent.getStringExtra("ALBUM_ART_URL")
        currentAudioUrl = intent.getStringExtra("AUDIO_URL")

        binding.tvSongTitle.text = songTitle
        binding.tvArtistName.text = artistName

        // --- 2. Cargar carátula con Glide ---
        albumArtUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .placeholder(android.R.drawable.ic_media_play)
                .error(android.R.drawable.ic_dialog_alert)
                .into(binding.ivAlbumArt)
        }

        // --- 3. Inicializar el Reproductor ---
        if (currentAudioUrl != null) {
            initializeMediaPlayer(currentAudioUrl!!)
        } else {
            Toast.makeText(this, "Error: No se encontró la URL de audio.", Toast.LENGTH_LONG).show()
            binding.ivPlayPause.isEnabled = false
        }

        // --- 4. Configurar botones ---
        binding.ivBackArrowPlayer.setOnClickListener { finish() }
        binding.ivPlayPause.setOnClickListener { togglePlayPause() }

        // --- 5. Configurar la SeekBar ---
        setupSeekBarListener()
    }

    private fun initializeMediaPlayer(url: String) {
        // Liberar si ya existía
        mediaPlayer?.release()

        val newPlayer = MediaPlayer()

        try {
            newPlayer.setDataSource(url)
            newPlayer.prepareAsync()

            newPlayer.setOnPreparedListener {
                Log.d(TAG, "MediaPlayer preparado. Duración: ${it.duration}")
                binding.seekbarProgress.max = it.duration
                binding.tvTotalTime.text = formatTime(it.duration)
                playAudio()
            }

            newPlayer.setOnCompletionListener {
                isPlaying = false
                binding.ivPlayPause.setImageResource(R.drawable.ic_play_arrow)
                stopUpdatingSeekBar()
                binding.seekbarProgress.progress = 0
                binding.tvCurrentTime.text = formatTime(0)
            }

            mediaPlayer = newPlayer // ✅ reasignamos fuera del apply

        } catch (e: IOException) {
            Log.e(TAG, "Error al configurar la fuente de datos: $e")
            Toast.makeText(this@PlayerActivity, "Error al cargar la canción.", Toast.LENGTH_LONG).show()
        }
    }


    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (isPlaying) {
                it.pause()
                binding.ivPlayPause.setImageResource(R.drawable.ic_play_arrow)
                stopUpdatingSeekBar()
            } else {
                it.start()
                binding.ivPlayPause.setImageResource(R.drawable.ic_pause)
                startUpdatingSeekBar()
            }
            // 🔧 'isPlaying' ahora es var, así que se puede reasignar sin error
            isPlaying = !isPlaying
        }
    }

    private fun playAudio() {
        mediaPlayer?.start()
        binding.ivPlayPause.setImageResource(R.drawable.ic_pause)
        isPlaying = true
        startUpdatingSeekBar()
    }

    private fun setupSeekBarListener() {
        binding.seekbarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvCurrentTime.text = formatTime(progress)
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopUpdatingSeekBar()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                startUpdatingSeekBar()
            }
        })
    }

    private fun startUpdatingSeekBar() {
        handler.post(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        binding.seekbarProgress.progress = it.currentPosition
                        binding.tvCurrentTime.text = formatTime(it.currentPosition)
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        })
    }

    private fun stopUpdatingSeekBar() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun formatTime(milliseconds: Int): String {
        return String.format(
            "%d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        stopUpdatingSeekBar()
    }
}
