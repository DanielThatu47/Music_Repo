package com.danielthatu.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielthatu.musicplayer.MainActivity
import com.danielthatu.musicplayer.adapters.SongsAdapter
import com.danielthatu.musicplayer.databinding.FragmentFavoritesBinding
import com.danielthatu.musicplayer.viewmodels.MusicViewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var adapter: SongsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        viewModel.loadFavorites()
    }

    private fun setupRecyclerView() {
        adapter = SongsAdapter(
            onSongClick = { song, position ->
                val songs = viewModel.favoriteSongs.value ?: return@SongsAdapter
                (activity as? MainActivity)?.playSongs(songs, position)
            },
            onMoreClick = { song, _ ->
                viewModel.toggleFavorite(song.id)
            }
        )
        binding.recyclerFavorites.apply {
            this.adapter = this@FavoritesFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.favoriteSongs.observe(viewLifecycleOwner) { songs ->
            adapter.submitList(songs)
            binding.tvEmpty.visibility = if (songs.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerFavorites.visibility = if (songs.isEmpty()) View.GONE else View.VISIBLE
        }

        // Reload whenever favorite set changes
        viewModel.favorites.observe(viewLifecycleOwner) {
            viewModel.loadFavorites()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
