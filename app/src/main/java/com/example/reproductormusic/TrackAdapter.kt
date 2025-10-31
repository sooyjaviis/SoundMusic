package com.example.reproductormusic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.reproductormusic.api.Track

// Se pasa la función onTrackClickListener en el constructor.
class TrackAdapter(
    private var tracks: List<Track>,
    private val onTrackClickListener: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // CORREGIDO: Usar los IDs exactos del XML
        val tvTrackName: TextView = itemView.findViewById(R.id.tv_track_title)
        val tvArtistName: TextView = itemView.findViewById(R.id.tv_track_artist)
        val ivTrackImage: ImageView = itemView.findViewById(R.id.iv_track_album_art)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track_result, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.tvTrackName.text = track.name
        holder.tvArtistName.text = track.artistName

        // Cargar imagen con Glide
        Glide.with(holder.itemView.context)
            .load(track.imageUrl)
            .placeholder(R.drawable.ic_music_placeholder) // Asegúrate de que este drawable exista
            .into(holder.ivTrackImage)

        holder.itemView.setOnClickListener {
            onTrackClickListener(track)
        }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}
