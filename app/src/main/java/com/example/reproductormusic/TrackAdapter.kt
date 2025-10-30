package com.example.reproductormusic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.reproductormusic.api.Track
// Asumimos que tu XML se llama item_track_result.xml
import com.example.reproductormusic.databinding.ItemTrackResultBinding

class TrackAdapter(
    private var tracks: List<Track>
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size

    inner class TrackViewHolder(private val binding: ItemTrackResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track) {

            // 🚨 CORREGIDO: Usando los IDs exactos de tu XML 🚨
            binding.tvTrackTitle.text = track.name
            binding.tvTrackArtist.text = track.artistName

            // Usamos Glide para cargar la imagen de la URL en el ImageView
            Glide.with(binding.ivTrackAlbumArt.context) // 🚨 CORREGIDO: Usando ivTrackAlbumArt 🚨
                .load(track.albumImageUrl)
                .placeholder(R.drawable.ic_sound_wave)
                .error(R.drawable.ic_sound_wave)
                .into(binding.ivTrackAlbumArt) // 🚨 CORREGIDO: Usando ivTrackAlbumArt 🚨

            binding.root.setOnClickListener {
                // Lógica para reproducir la canción
            }
        }
    }
}