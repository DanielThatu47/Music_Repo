package com.danielthatu.musicplayer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.danielthatu.musicplayer.models.Playlist
import com.danielthatu.musicplayer.models.Song
import com.danielthatu.musicplayer.utils.MusicRepository
import com.danielthatu.musicplayer.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)

    // ——— All songs ————————————————————————————————————————————
    private val _allSongs = MutableLiveData<List<Song>>()
    val allSongs: LiveData<List<Song>> = _allSongs

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // ——— Current queue ————————————————————————————————————————
    private val _currentQueue = MutableLiveData<List<Song>>()
    val currentQueue: LiveData<List<Song>> = _currentQueue

    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    // ——— Search ————————————————————————————————————————————————
    private val _searchResults = MutableLiveData<List<Song>>()
    val searchResults: LiveData<List<Song>> = _searchResults

    // ——— Favorites ————————————————————————————————————————————
    val favorites = repository.getFavoritesLive()

    private val _favoriteSongs = MutableLiveData<List<Song>>()
    val favoriteSongs: LiveData<List<Song>> = _favoriteSongs

    // ——— Playlists ————————————————————————————————————————————
    val playlists: LiveData<List<Playlist>> = repository.getAllPlaylistsLive()

    private val _playlistSongs = MutableLiveData<List<Song>>()
    val playlistSongs: LiveData<List<Song>> = _playlistSongs

    init {
        loadAllSongs()
    }

    fun loadAllSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val songs = repository.getAllSongs()
            val sorted = sortSongs(songs)
            _allSongs.postValue(sorted)
            _isLoading.postValue(false)
        }
    }

    private fun sortSongs(songs: List<Song>): List<Song> {
        val order = PreferenceManager.getSortOrder(getApplication())
        return when (order) {
            PreferenceManager.SORT_BY_TITLE -> songs.sortedBy { it.title.lowercase() }
            PreferenceManager.SORT_BY_ARTIST -> songs.sortedBy { it.artist.lowercase() }
            PreferenceManager.SORT_BY_ALBUM -> songs.sortedBy { it.album.lowercase() }
            PreferenceManager.SORT_BY_DATE -> songs.sortedByDescending { it.dateAdded }
            PreferenceManager.SORT_BY_DURATION -> songs.sortedByDescending { it.duration }
            else -> songs.sortedBy { it.title.lowercase() }
        }
    }

    fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isBlank()) {
                _searchResults.postValue(emptyList())
            } else {
                val results = repository.searchSongs(query)
                _searchResults.postValue(results)
            }
        }
    }

    fun setCurrentSong(song: Song) {
        _currentSong.value = song
    }

    fun setPlayingState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    fun setCurrentQueue(songs: List<Song>) {
        _currentQueue.value = songs
    }

    fun loadFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            val songs = repository.getFavoriteSongs()
            _favoriteSongs.postValue(songs)
        }
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.isFavorite(songId)) {
                repository.removeFavorite(songId)
            } else {
                repository.addFavorite(songId)
            }
        }
    }

    fun isFavoriteLive(songId: Long) = repository.isFavoriteLive(songId)

    fun createPlaylist(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePlaylist(playlist)
        }
    }

    fun renamePlaylist(playlist: Playlist, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.renamePlaylist(playlist, newName)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun loadPlaylistSongs(playlistId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val songs = repository.getPlaylistSongs(playlistId)
            _playlistSongs.postValue(songs)
        }
    }

    fun setSortOrder(order: String) {
        PreferenceManager.setSortOrder(getApplication(), order)
        loadAllSongs()
    }
}
