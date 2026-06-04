package com.danielthatu.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.danielthatu.musicplayer.databinding.ActivityMainBinding
import com.danielthatu.musicplayer.models.Song
import com.danielthatu.musicplayer.services.MusicService
import com.danielthatu.musicplayer.viewmodels.MusicViewModel
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    val viewModel: MusicViewModel by viewModels()

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    val controller get() = if (::controllerFuture.isInitialized && controllerFuture.isDone) controllerFuture.get() else null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms.values.any { it }
        if (granted) {
            viewModel.loadAllSongs()
        } else {
            showPermissionDeniedState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupMiniPlayer()
        checkAndRequestPermissions()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.songsFragment, R.id.favoritesFragment,
                R.id.playlistsFragment, R.id.searchFragment,
                R.id.settingsFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
                else -> { /* keep visible */ }
            }
        }
    }

    private fun setupMiniPlayer() {
        binding.miniPlayerLayout.visibility = View.GONE

        viewModel.currentSong.observe(this) { song ->
            if (song != null) showMiniPlayer(song)
            else binding.miniPlayerLayout.visibility = View.GONE
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            binding.btnMiniPlayPause.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        binding.miniPlayerLayout.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }
        binding.btnMiniPlayPause.setOnClickListener {
            controller?.let { if (it.isPlaying) it.pause() else it.play() }
        }
        binding.btnMiniNext.setOnClickListener {
            controller?.seekToNextMediaItem()
        }
    }

    private fun showMiniPlayer(song: Song) {
        binding.miniPlayerLayout.visibility = View.VISIBLE
        binding.tvMiniTitle.text = song.title
        binding.tvMiniArtist.text = song.artist
        Glide.with(this)
            .load(song.albumArtUri)
            .placeholder(R.drawable.ic_music_note)
            .error(R.drawable.ic_music_note)
            .circleCrop()
            .into(binding.ivMiniAlbumArt)
    }

    fun checkAndRequestPermissions() {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
                add(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            viewModel.loadAllSongs()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    private fun showPermissionDeniedState() {
        // ViewModel exposes this state to fragments
        viewModel.setPermissionDenied(true)
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        val c = controller ?: return
        val items = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArtUri)
                        .build()
                ).build()
        }
        c.setMediaItems(items, startIndex, 0L)
        c.prepare()
        c.play()
        viewModel.setCurrentSong(songs[startIndex])
        viewModel.setCurrentQueue(songs)
        viewModel.addToRecentlyPlayed(songs[startIndex])
    }

    override fun onStart() {
        super.onStart()
        val token = SessionToken(this, ComponentName(this, MusicService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()
        controllerFuture.addListener({
            val c = controllerFuture.get()
            c.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    viewModel.setPlayingState(isPlaying)
                }
                override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                    item?.mediaId?.toLongOrNull()?.let { id ->
                        viewModel.allSongs.value?.find { it.id == id }?.let { song ->
                            viewModel.setCurrentSong(song)
                            viewModel.addToRecentlyPlayed(song)
                        }
                    }
                }
            })
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        MediaController.releaseFuture(controllerFuture)
        super.onStop()
    }
}
