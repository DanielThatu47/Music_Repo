package com.danielthatu.musicplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.danielthatu.musicplayer.R
import com.danielthatu.musicplayer.models.Song

class SongsAdapter(
    private val onSongClick: (Song, Int) -> Unit,
    private val onMoreClick: (Song, View) -> Unit
) : ListAdapter<Song, SongsAdapter.SongViewHolder>(SongDiffCallback()) {

    private var highlightedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position), position == highlightedPosition)
    }

    fun setHighlightedPosition(position: Int) {
        val previous = highlightedPosition
        highlightedPosition = position
        if (previous != -1) notifyItemChanged(previous)
        if (position != -1) notifyItemChanged(position)
    }

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumArt: ImageView = itemView.findViewById(R.id.iv_album_art)
        private val title: TextView = itemView.findViewById(R.id.tv_song_title)
        private val artist: TextView = itemView.findViewById(R.id.tv_artist)
        private val duration: TextView = itemView.findViewById(R.id.tv_duration)
        private val moreBtn: ImageButton = itemView.findViewById(R.id.btn_more)
        private val nowPlayingIndicator: View = itemView.findViewById(R.id.view_now_playing)

        fun bind(song: Song, isHighlighted: Boolean) {
            title.text = song.title
            artist.text = song.artist
            duration.text = song.formattedDuration

            nowPlayingIndicator.visibility = if (isHighlighted) View.VISIBLE else View.INVISIBLE
            title.setTextColor(
                if (isHighlighted)
                    itemView.context.getColor(R.color.colorPrimary)
                else
                    itemView.context.getColor(R.color.text_primary)
            )

            Glide.with(itemView.context)
                .load(song.albumArtUri)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .centerCrop()
                .into(albumArt)

            itemView.setOnClickListener { onSongClick(song, adapterPosition) }
            moreBtn.setOnClickListener { onMoreClick(song, it) }
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Song, newItem: Song) = oldItem == newItem
    }
}
