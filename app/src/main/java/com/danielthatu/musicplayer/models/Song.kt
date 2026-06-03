package com.danielthatu.musicplayer.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val path: String,
    val uri: Uri,
    val size: Long,
    val dateAdded: Long,
    val trackNumber: Int = 0,
    var isFavorite: Boolean = false
) : Parcelable {
    val albumArtUri: Uri
        get() = Uri.parse("content://media/external/audio/albumart/$albumId")

    val formattedDuration: String
        get() {
            val totalSecs = duration / 1000
            val mins = totalSecs / 60
            val secs = totalSecs % 60
            return String.format("%d:%02d", mins, secs)
        }
}
