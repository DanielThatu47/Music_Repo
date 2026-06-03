package com.danielthatu.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielthatu.musicplayer.MainActivity
import com.danielthatu.musicplayer.adapters.SongsAdapter
import com.danielthatu.musicplayer.databinding.FragmentPlaylistDetailBinding
import com.danielthatu.musicplayer.viewmodels.MusicViewModel

class PlaylistDetailFragment : Fragment() {

    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var adapter: SongsAdapter
    private var playlistId: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistId = arguments?.getLong("playlistId") ?: -1L
        val playlistName = arguments?.getString("playlistName") ?: "Playlist"

        binding.toolbar.title = playlistName
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        setupRecyclerView()
        observeViewModel()

        if (playlistId != -1L) viewModel.loadPlaylistSongs(playlistId)
    }

    private fun setupRecyclerView() {
        adapter = SongsAdapter(
            onSongClick = { song, position ->
                val songs = viewModel.playlistSongs.value ?: return@SongsAdapter
                (activity as? MainActivity)?.playSongs(songs, position)
            },
            onMoreClick = { song, _ ->
                viewModel.removeSongFromPlaylist(playlistId, song.id)
            }
        )
        binding.recyclerSongs.apply {
            this.adapter = this@PlaylistDetailFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.playlistSongs.observe(viewLifecycleOwner) { songs ->
            adapter.submitList(songs)
            binding.tvEmpty.visibility = if (songs.isEmpty()) View.VISIBLE else View.GONE
            binding.tvSongCount.text = "${songs.size} songs"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
