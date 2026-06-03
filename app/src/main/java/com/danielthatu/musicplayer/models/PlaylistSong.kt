package com.danielthatu.musicplayer.models

import androidx.room.Entity

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSong(
    val playlistId: Long,
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis(),
    val order: Int = 0
)
