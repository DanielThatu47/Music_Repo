package com.danielthatu.musicplayer.fragments

import android.os.Bundle
import android.view.*
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SongsFragment : Fragment() {

    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var songsAdapter: SongsAdapter
    private var currentHighlight = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, b: Bundle?): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupMenu()
        observeViewModel()
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadAllSongs() }
        binding.btnGrantPermission.setOnClickListener {
            (activity as? MainActivity)?.checkAndRequestPermissions()
        }
        binding.btnShuffleAll.setOnClickListener {
            val songs = viewModel.allSongs.value ?: return@setOnClickListener
            if (songs.isEmpty()) return@setOnClickListener
            val shuffled = songs.shuffled()
            (activity as? MainActivity)?.playSongs(shuffled, 0)
        }
    }

    private fun setupRecyclerView() {
        songsAdapter = SongsAdapter(
            onSongClick = { song, position ->
                val songs = viewModel.allSongs.value ?: return@SongsAdapter
                (activity as? MainActivity)?.playSongs(songs, position)
                songsAdapter.setHighlightedPosition(position)
                currentHighlight = position
            },
            onMoreClick = { song, anchor -> showSongMenu(song, anchor) }
        )
        binding.recyclerSongs.apply {
            adapter = songsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inf: MenuInflater) {
                inf.inflate(R.menu.songs_menu, menu)
            }
            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.sort_by_title -> { viewModel.setSortOrder(PreferenceManager.SORT_BY_TITLE); true }
                    R.id.sort_by_artist -> { viewModel.setSortOrder(PreferenceManager.SORT_BY_ARTIST); true }
                    R.id.sort_by_album -> { viewModel.setSortOrder(PreferenceManager.SORT_BY_ALBUM); true }
                    R.id.sort_by_date -> { viewModel.setSortOrder(PreferenceManager.SORT_BY_DATE); true }
                    R.id.sort_by_duration -> { viewModel.setSortOrder(PreferenceManager.SORT_BY_DURATION); true }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeViewModel() {
        viewModel.allSongs.observe(viewLifecycleOwner) { songs ->
            songsAdapter.submitList(songs)
            binding.swipeRefresh.isRefreshing = false
            val isEmpty = songs.isEmpty()
            binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerSongs.visibility = if (isEmpty) View.GONE else View.VISIBLE
            if (!isEmpty) {
                binding.tvSongCount.text = "${songs.size} songs"
                binding.btnShuffleAll.visibility = View.VISIBLE
                binding.permissionState.visibility = View.GONE
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) binding.swipeRefresh.isRefreshing = true
        }
        viewModel.permissionDenied.observe(viewLifecycleOwner) { denied ->
            if (denied) {
                binding.permissionState.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                binding.recyclerSongs.visibility = View.GONE
                binding.btnShuffleAll.visibility = View.GONE
            }
        }
        viewModel.currentSong.observe(viewLifecycleOwner) { song ->
            song?.let {
                val pos = viewModel.allSongs.value?.indexOfFirst { s -> s.id == it.id } ?: -1
                if (pos != currentHighlight) {
                    songsAdapter.setHighlightedPosition(pos)
                    currentHighlight = pos
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
                        val pos = songs.indexOfFirst { it.id == song.id }
                        (activity as? MainActivity)?.playSongs(songs, pos)
                        true
                    }
                    R.id.action_add_to_playlist -> {
                        showAddToPlaylistDialog(song)
                        true
                    }
                    R.id.action_favorite -> {
                        viewModel.toggleFavorite(song.id)
                        requireContext().showToast("Added to Liked Songs")
                        true
                    }
                    R.id.action_share -> {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "audio/*"
                            putExtra(android.content.Intent.EXTRA_STREAM, song.uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(android.content.Intent.createChooser(intent, "Share ${song.title}"))
                        true
                    }
                    R.id.action_song_info -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(song.title)
                            .setMessage("Artist: ${song.artist}\nAlbum: ${song.album}\nDuration: ${song.formattedDuration}\nSize: ${song.size.toFormattedSize()}\nPath: ${song.path}")
                            .setPositiveButton("OK", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showAddToPlaylistDialog(song: Song) {
        val playlists = viewModel.playlists.value ?: emptyList()
        if (playlists.isEmpty()) {
            requireContext().showToast("No playlists yet — create one in Library")
            return
        }
        val names = playlists.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add to Playlist")
            .setItems(names) { _, which ->
                viewModel.addSongToPlaylist(playlists[which].id, song.id)
                requireContext().showToast("Added to ${playlists[which].name}")
            }
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
