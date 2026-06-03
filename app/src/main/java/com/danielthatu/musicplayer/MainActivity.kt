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
import com.danielthatu.musicplayer.utils.showToast
import com.danielthatu.musicplayer.viewmodels.MusicViewModel
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MusicViewModel by viewModels()

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller get() = if (controllerFuture.isDone) controllerFuture.get() else null

    // Permission request
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.loadAllSongs()
        } else {
            showToast("Storage permission required to read music")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupMiniPlayer()
        checkPermissions()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun setupMiniPlayer() {
        binding.miniPlayerLayout.visibility = View.GONE

        viewModel.currentSong.observe(this) { song ->
            song?.let {
                showMiniPlayer(it)
            } ?: run {
                binding.miniPlayerLayout.visibility = View.GONE
            }
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
            controller?.let { c ->
                if (c.isPlaying) c.pause() else c.play()
            }
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
            .centerCrop()
            .into(binding.ivMiniAlbumArt)
    }

    private fun checkPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            viewModel.loadAllSongs()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        val controller = controller ?: return
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArtUri)
                        .build()
                )
                .build()
        }
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
        viewModel.setCurrentSong(songs[startIndex])
        viewModel.setCurrentQueue(songs)
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    viewModel.setPlayingState(isPlaying)
                }
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    // Update current song when track changes
                    mediaItem?.let {
                        val songId = it.mediaId.toLongOrNull() ?: return
                        viewModel.allSongs.value?.find { s -> s.id == songId }?.let { song ->
                            viewModel.setCurrentSong(song)
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
