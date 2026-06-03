package com.danielthatu.musicplayer

import android.content.ComponentName
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.danielthatu.musicplayer.databinding.ActivityPlayerBinding
import com.danielthatu.musicplayer.models.Song
import com.danielthatu.musicplayer.services.MusicService
import com.danielthatu.musicplayer.utils.toFormattedTime
import com.danielthatu.musicplayer.viewmodels.MusicViewModel
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: MusicViewModel by viewModels()

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller get() = if (controllerFuture.isDone) controllerFuture.get() else null

    private var seekJob: Job? = null
    private var currentSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupControls()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun observeViewModel() {
        viewModel.currentSong.observe(this) { song ->
            song?.let { updateUI(it) }
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            binding.btnPlayPause.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
            if (isPlaying) startSeekBarUpdate() else stopSeekBarUpdate()
        }

        viewModel.isFavoriteLive(currentSong?.id ?: -1L).observe(this) { isFav ->
            binding.btnFavorite.setImageResource(
                if (isFav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline
            )
        }
    }

    private fun setupControls() {
        binding.btnPlayPause.setOnClickListener {
            controller?.let { c -> if (c.isPlaying) c.pause() else c.play() }
        }

        binding.btnNext.setOnClickListener { controller?.seekToNextMediaItem() }
        binding.btnPrevious.setOnClickListener { controller?.seekToPreviousMediaItem() }

        binding.btnShuffle.setOnClickListener {
            controller?.let { c ->
                c.shuffleModeEnabled = !c.shuffleModeEnabled
                updateShuffleIcon(c.shuffleModeEnabled)
            }
        }

        binding.btnRepeat.setOnClickListener {
            controller?.let { c ->
                c.repeatMode = when (c.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
                updateRepeatIcon(c.repeatMode)
            }
        }

        binding.btnFavorite.setOnClickListener {
            currentSong?.let { song ->
                viewModel.toggleFavorite(song.id)
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.tvCurrentTime.text = progress.toLong().toFormattedTime()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = stopSeekBarUpdate()

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let { controller?.seekTo(it.progress.toLong()) }
                startSeekBarUpdate()
            }
        })
    }

    private fun updateUI(song: Song) {
        currentSong = song
        binding.tvSongTitle.text = song.title
        binding.tvArtistName.text = song.artist
        binding.tvAlbumName.text = song.album
        binding.tvTotalTime.text = song.duration.toFormattedTime()
        binding.seekBar.max = song.duration.toInt()

        Glide.with(this)
            .load(song.albumArtUri)
            .placeholder(R.drawable.ic_music_note)
            .error(R.drawable.ic_music_note)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean = false
                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean = false
            })
            .into(binding.ivAlbumArt)
    }

    private fun startSeekBarUpdate() {
        seekJob?.cancel()
        seekJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                controller?.let { c ->
                    if (c.isPlaying) {
                        val position = c.currentPosition
                        binding.seekBar.progress = position.toInt()
                        binding.tvCurrentTime.text = position.toFormattedTime()
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopSeekBarUpdate() {
        seekJob?.cancel()
        seekJob = null
    }

    private fun updateShuffleIcon(enabled: Boolean) {
        binding.btnShuffle.alpha = if (enabled) 1.0f else 0.5f
        binding.btnShuffle.setColorFilter(
            if (enabled) ContextCompat.getColor(this, R.color.colorPrimary)
            else ContextCompat.getColor(this, R.color.icon_default)
        )
    }

    private fun updateRepeatIcon(mode: Int) {
        when (mode) {
            Player.REPEAT_MODE_OFF -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
                binding.btnRepeat.alpha = 0.5f
            }
            Player.REPEAT_MODE_ALL -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
                binding.btnRepeat.alpha = 1.0f
                binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
            }
            Player.REPEAT_MODE_ONE -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
                binding.btnRepeat.alpha = 1.0f
                binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
            val c = controllerFuture.get()
            c.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    viewModel.setPlayingState(isPlaying)
                }
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    mediaItem?.let {
                        val songId = it.mediaId.toLongOrNull() ?: return
                        viewModel.allSongs.value?.find { s -> s.id == songId }?.let { song ->
                            viewModel.setCurrentSong(song)
                        }
                    }
                }
            })
            // Sync initial state
            if (c.isPlaying) startSeekBarUpdate()
            updateShuffleIcon(c.shuffleModeEnabled)
            updateRepeatIcon(c.repeatMode)
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        stopSeekBarUpdate()
        MediaController.releaseFuture(controllerFuture)
        super.onStop()
    }
}
