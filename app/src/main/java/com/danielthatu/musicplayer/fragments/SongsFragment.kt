package com.danielthatu.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielthatu.musicplayer.MainActivity
import com.danielthatu.musicplayer.R
import com.danielthatu.musicplayer.adapters.SongsAdapter
import com.danielthatu.musicplayer.databinding.FragmentSongsBinding
import com.danielthatu.musicplayer.models.Song
import com.danielthatu.musicplayer.utils.PreferenceManager
import com.danielthatu.musicplayer.utils.showToast
import com.danielthatu.musicplayer.utils.toFormattedSize
import com.danielthatu.musicplayer.viewmodels.MusicViewModel

class SongsFragment : Fragment() {

    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var songsAdapter: SongsAdapter
    private var currentHighlightPosition = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupMenu()
        observeViewModel()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadAllSongs()
        }
    }

    private fun setupRecyclerView() {
        songsAdapter = SongsAdapter(
            onSongClick = { song, position ->
                val songs = viewModel.allSongs.value ?: return@SongsAdapter
                (activity as? MainActivity)?.playSongs(songs, position)
                songsAdapter.setHighlightedPosition(position)
                currentHighlightPosition = position
            },
            onMoreClick = { song, view ->
                showSongMenu(song, view)
            }
        )
        binding.recyclerSongs.apply {
            adapter = songsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.songs_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.sort_by_title -> {
                        viewModel.setSortOrder(PreferenceManager.SORT_BY_TITLE)
                        true
                    }
                    R.id.sort_by_artist -> {
                        viewModel.setSortOrder(PreferenceManager.SORT_BY_ARTIST)
                        true
                    }
                    R.id.sort_by_album -> {
                        viewModel.setSortOrder(PreferenceManager.SORT_BY_ALBUM)
                        true
                    }
                    R.id.sort_by_date -> {
                        viewModel.setSortOrder(PreferenceManager.SORT_BY_DATE)
                        true
                    }
                    R.id.sort_by_duration -> {
                        viewModel.setSortOrder(PreferenceManager.SORT_BY_DURATION)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeViewModel() {
        viewModel.allSongs.observe(viewLifecycleOwner) { songs ->
            songsAdapter.submitList(songs)
            binding.swipeRefresh.isRefreshing = false
            binding.tvEmptyState.visibility = if (songs.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerSongs.visibility = if (songs.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) binding.swipeRefresh.isRefreshing = true
        }

        viewModel.currentSong.observe(viewLifecycleOwner) { song ->
            song?.let {
                val position = viewModel.allSongs.value?.indexOfFirst { s -> s.id == it.id } ?: -1
                if (position != currentHighlightPosition) {
                    songsAdapter.setHighlightedPosition(position)
                    currentHighlightPosition = position
                }
            }
        }
    }

    private fun showSongMenu(song: Song, anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            menuInflater.inflate(R.menu.song_item_menu, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_play -> {
                        val songs = viewModel.allSongs.value ?: return@setOnMenuItemClickListener true
                        val position = songs.indexOfFirst { it.id == song.id }
                        (activity as? MainActivity)?.playSongs(songs, position)
                        true
                    }
                    R.id.action_add_to_playlist -> {
                        // Navigate to playlist picker
                        requireContext().showToast("Add to playlist - coming soon")
                        true
                    }
                    R.id.action_favorite -> {
                        viewModel.toggleFavorite(song.id)
                        true
                    }
                    R.id.action_share -> {
                        // Share intent
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "audio/*"
                            putExtra(android.content.Intent.EXTRA_STREAM, song.uri)
                        }
                        startActivity(android.content.Intent.createChooser(shareIntent, "Share ${song.title}"))
                        true
                    }
                    R.id.action_song_info -> {
                        showSongInfo(song)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showSongInfo(song: Song) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(song.title)
            .setMessage(
                "Artist: ${song.artist}\n" +
                "Album: ${song.album}\n" +
                "Duration: ${song.formattedDuration}\n" +
                "Size: ${song.size.toFormattedSize()}\n" +
                "Path: ${song.path}"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
