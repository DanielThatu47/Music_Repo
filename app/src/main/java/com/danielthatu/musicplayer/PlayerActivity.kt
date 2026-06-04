package com.danielthatu.musicplayer

import android.content.ComponentName
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: MusicViewModel by viewModels()

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller get() = if (::controllerFuture.isInitialized && controllerFuture.isDone) controllerFuture.get() else null

    private val handler = Handler(Looper.getMainLooper())
    private var seekRunnable: Runnable? = null
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
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun observeViewModel() {
        viewModel.currentSong.observe(this) { song ->
            song?.let { updateUI(it) }
        }
        viewModel.isPlaying.observe(this) { playing ->
            binding.btnPlayPause.setImageResource(
                if (playing) R.drawable.ic_pause_circle else R.drawable.ic_play_circle
            )
            if (playing) startSeek() else stopSeek()
        }
        viewModel.isFavoriteLive(currentSong?.id ?: -1L).observe(this) { fav ->
            binding.btnFavorite.setImageResource(
                if (fav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline
            )
            binding.btnFavorite.setColorFilter(
                ContextCompat.getColor(this, if (fav) R.color.favorite_red else R.color.icon_default)
            )
        }
    }

    private fun setupControls() {
        binding.btnPlayPause.setOnClickListener {
            controller?.let { if (it.isPlaying) it.pause() else it.play() }
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
            currentSong?.let { viewModel.toggleFavorite(it.id) }
        }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) binding.tvCurrentTime.text = progress.toLong().toFormattedTime()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) = stopSeek()
            override fun onStopTrackingTouch(sb: SeekBar?) {
                sb?.let { controller?.seekTo(it.progress.toLong()) }
                startSeek()
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
                override fun onLoadFailed(e: GlideException?, m: Any?, t: Target<Drawable>, f: Boolean) = false
                override fun onResourceReady(r: Drawable, m: Any, t: Target<Drawable>, d: DataSource, f: Boolean) = false
            })
            .into(binding.ivAlbumArt)
    }

    private fun startSeek() {
        seekRunnable?.let { handler.removeCallbacks(it) }
        seekRunnable = object : Runnable {
            override fun run() {
                controller?.let { c ->
                    if (c.isPlaying) {
                        val pos = c.currentPosition
                        binding.seekBar.progress = pos.toInt()
                        binding.tvCurrentTime.text = pos.toFormattedTime()
                    }
                }
                handler.postDelayed(this, 500)
            }
        }
        handler.post(seekRunnable!!)
    }

    private fun stopSeek() {
        seekRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun updateShuffleIcon(enabled: Boolean) {
        val color = if (enabled) R.color.spotify_green else R.color.icon_default
        binding.btnShuffle.setColorFilter(ContextCompat.getColor(this, color))
        binding.btnShuffle.alpha = if (enabled) 1f else 0.5f
    }

    private fun updateRepeatIcon(mode: Int) {
        when (mode) {
            Player.REPEAT_MODE_OFF -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
                binding.btnRepeat.alpha = 0.5f
                binding.btnRepeat.clearColorFilter()
            }
            Player.REPEAT_MODE_ALL -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
                binding.btnRepeat.alpha = 1f
                binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.spotify_green))
            }
            Player.REPEAT_MODE_ONE -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
                binding.btnRepeat.alpha = 1f
                binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.spotify_green))
            }
        }
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
                        }
                    }
                }
            })
            if (c.isPlaying) startSeek()
            updateShuffleIcon(c.shuffleModeEnabled)
            updateRepeatIcon(c.repeatMode)
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        stopSeek()
        MediaController.releaseFuture(controllerFuture)
        super.onStop()
    }
}
