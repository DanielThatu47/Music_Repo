package com.danielthatu.musicplayer.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.danielthatu.musicplayer.models.FavoriteSong
import com.danielthatu.musicplayer.models.Playlist
import com.danielthatu.musicplayer.models.PlaylistSong

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): LiveData<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): Playlist?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(playlistSong: PlaylistSong)

    @Delete
    suspend fun removeSongFromPlaylist(playlistSong: PlaylistSong)

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY `order`")
    suspend fun getSongIdsForPlaylist(playlistId: Long): List<Long>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getSongCountForPlaylist(playlistId: Long): Int

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId)")
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean
}

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favoriteSong: FavoriteSong)

    @Delete
    suspend fun removeFavorite(favoriteSong: FavoriteSong)

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): LiveData<List<FavoriteSong>>

    @Query("SELECT songId FROM favorites ORDER BY addedAt DESC")
    suspend fun getAllFavoriteSongIds(): List<Long>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    suspend fun isFavorite(songId: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavoriteLive(songId: Long): LiveData<Boolean>
}
