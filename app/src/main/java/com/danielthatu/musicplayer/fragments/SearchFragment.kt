package com.danielthatu.musicplayer.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielthatu.musicplayer.MainActivity
import com.danielthatu.musicplayer.adapters.SongsAdapter
import com.danielthatu.musicplayer.databinding.FragmentSearchBinding
import com.danielthatu.musicplayer.viewmodels.MusicViewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var adapter: SongsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchInput()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupSearchInput() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                viewModel.search(query)
                binding.btnClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClear.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        // Auto-focus keyboard
        binding.etSearch.requestFocus()
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setupRecyclerView() {
        adapter = SongsAdapter(
            onSongClick = { song, position ->
                val songs = viewModel.searchResults.value ?: return@SongsAdapter
                (activity as? MainActivity)?.playSongs(songs, position)
            },
            onMoreClick = { song, _ ->
                viewModel.toggleFavorite(song.id)
            }
        )
        binding.recyclerResults.apply {
            this.adapter = this@SearchFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            adapter.submitList(results)
            val hasQuery = binding.etSearch.text?.isNotEmpty() == true
            binding.tvNoResults.visibility =
                if (hasQuery && results.isEmpty()) View.VISIBLE else View.GONE
            binding.tvHint.visibility =
                if (!hasQuery) View.VISIBLE else View.GONE
            binding.recyclerResults.visibility =
                if (results.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
