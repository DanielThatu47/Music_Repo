package com.danielthatu.musicplayer.utils

import android.content.Context
import androidx.lifecycle.LiveData
import com.danielthatu.musicplayer.database.MusicDatabase
import com.danielthatu.musicplayer.models.FavoriteSong
import com.danielthatu.musicplayer.models.Playlist
import com.danielthatu.musicplayer.models.PlaylistSong
import com.danielthatu.musicplayer.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository(context: Context) {

    private val db = MusicDatabase.getInstance(context)
    private val playlistDao = db.playlistDao()
    private val favoriteDao = db.favoriteDao()
    private val appContext = context.applicationContext

    // ——— Songs ———————————————————————————————————————————————

    suspend fun getAllSongs(): List<Song> = withContext(Dispatchers.IO) {
        MediaStoreHelper.getAllSongs(appContext)
    }

    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        MediaStoreHelper.searchSongs(appContext, query)
    }

    // ——— Favorites ———————————————————————————————————————————

    fun getFavoritesLive(): LiveData<List<FavoriteSong>> = favoriteDao.getAllFavorites()

    suspend fun addFavorite(songId: Long) {
        favoriteDao.addFavorite(FavoriteSong(songId))
    }

    suspend fun removeFavorite(songId: Long) {
        favoriteDao.removeFavorite(FavoriteSong(songId))
    }

    suspend fun isFavorite(songId: Long): Boolean = favoriteDao.isFavorite(songId)

    fun isFavoriteLive(songId: Long): LiveData<Boolean> = favoriteDao.isFavoriteLive(songId)

    suspend fun getFavoriteSongs(): List<Song> = withContext(Dispatchers.IO) {
        val favIds = favoriteDao.getAllFavoriteSongIds().toSet()
        val allSongs = MediaStoreHelper.getAllSongs(appContext)
        allSongs.filter { it.id in favIds }.map { it.copy(isFavorite = true) }
    }

    // ——— Playlists ————————————————————————————————————————————

    fun getAllPlaylistsLive(): LiveData<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.createPlaylist(Playlist(name = name))
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.clearPlaylist(playlist.id)
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun renamePlaylist(playlist: Playlist, newName: String) {
        playlistDao.updatePlaylist(playlist.copy(name = newName))
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        if (!playlistDao.isSongInPlaylist(playlistId, songId)) {
            val count = playlistDao.getSongCountForPlaylist(playlistId)
            playlistDao.addSongToPlaylist(PlaylistSong(playlistId, songId, order = count))
            val playlist = playlistDao.getPlaylistById(playlistId)
            playlist?.let { playlistDao.updatePlaylist(it.copy(songCount = count + 1)) }
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(PlaylistSong(playlistId, songId))
        val count = playlistDao.getSongCountForPlaylist(playlistId)
        val playlist = playlistDao.getPlaylistById(playlistId)
        playlist?.let { playlistDao.updatePlaylist(it.copy(songCount = count)) }
    }

    suspend fun getPlaylistSongs(playlistId: Long): List<Song> = withContext(Dispatchers.IO) {
        val songIds = playlistDao.getSongIdsForPlaylist(playlistId).toSet()
        val allSongs = MediaStoreHelper.getAllSongs(appContext)
        val songMap = allSongs.associateBy { it.id }
        songIds.mapNotNull { songMap[it] }
    }
}
