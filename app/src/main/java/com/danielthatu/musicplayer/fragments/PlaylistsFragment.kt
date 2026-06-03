package com.danielthatu.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielthatu.musicplayer.R
import com.danielthatu.musicplayer.adapters.PlaylistAdapter
import com.danielthatu.musicplayer.databinding.FragmentPlaylistsBinding
import com.danielthatu.musicplayer.models.Playlist
import com.danielthatu.musicplayer.utils.showToast
import com.danielthatu.musicplayer.viewmodels.MusicViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var adapter: PlaylistAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        setupMenu()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = PlaylistAdapter(
            onPlaylistClick = { playlist ->
                // Navigate to playlist detail
                val bundle = Bundle().apply {
                    putLong("playlistId", playlist.id)
                    putString("playlistName", playlist.name)
                }
                findNavController().navigate(R.id.action_playlists_to_playlistDetail, bundle)
            },
            onMoreClick = { playlist, anchor ->
                showPlaylistMenu(playlist, anchor)
            }
        )
        binding.recyclerPlaylists.apply {
            this.adapter = this@PlaylistsFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        binding.fabNewPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}
            override fun onMenuItemSelected(item: MenuItem) = false
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeViewModel() {
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            adapter.submitList(playlists)
            binding.tvEmpty.visibility = if (playlists.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerPlaylists.visibility = if (playlists.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun showCreatePlaylistDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "Playlist name"
            setPadding(48, 24, 48, 0)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("New Playlist")
            .setView(editText)
            .setPositiveButton("Create") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.createPlaylist(name)
                    requireContext().showToast("Playlist '$name' created")
                } else {
                    requireContext().showToast("Name cannot be empty")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPlaylistMenu(playlist: Playlist, anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            inflate(R.menu.playlist_item_menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_rename -> {
                        showRenameDialog(playlist)
                        true
                    }
                    R.id.action_delete -> {
                        showDeleteConfirmation(playlist)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showRenameDialog(playlist: Playlist) {
        val editText = EditText(requireContext()).apply {
            hint = "Playlist name"
            setText(playlist.name)
            setPadding(48, 24, 48, 0)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Rename Playlist")
            .setView(editText)
            .setPositiveButton("Rename") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.renamePlaylist(playlist, name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(playlist: Playlist) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Playlist")
            .setMessage("Delete \"${playlist.name}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePlaylist(playlist)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
