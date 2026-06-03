package com.danielthatu.musicplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danielthatu.musicplayer.R
import com.danielthatu.musicplayer.models.Playlist

class PlaylistAdapter(
    private val onPlaylistClick: (Playlist) -> Unit,
    private val onMoreClick: (Playlist, View) -> Unit
) : ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tv_playlist_name)
        private val songCount: TextView = itemView.findViewById(R.id.tv_song_count)
        private val moreBtn: ImageButton = itemView.findViewById(R.id.btn_playlist_more)

        fun bind(playlist: Playlist) {
            name.text = playlist.name
            songCount.text = itemView.context.resources.getQuantityString(
                R.plurals.song_count, playlist.songCount, playlist.songCount
            )
            itemView.setOnClickListener { onPlaylistClick(playlist) }
            moreBtn.setOnClickListener { onMoreClick(playlist, it) }
        }
    }

    class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist) = oldItem == newItem
    }
}
