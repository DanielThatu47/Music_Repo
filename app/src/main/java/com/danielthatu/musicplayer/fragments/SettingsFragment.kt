package com.danielthatu.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.danielthatu.musicplayer.databinding.FragmentSettingsBinding
import com.danielthatu.musicplayer.utils.PreferenceManager
import com.danielthatu.musicplayer.utils.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupThemeSetting()
        setupSleepTimer()
        setupAbout()
    }

    private fun setupThemeSetting() {
        val currentTheme = PreferenceManager.getTheme(requireContext())
        binding.tvCurrentTheme.text = when (currentTheme) {
            PreferenceManager.THEME_LIGHT -> "Light"
            PreferenceManager.THEME_DARK -> "Dark"
            else -> "System default"
        }
        binding.rowTheme.setOnClickListener {
            val options = arrayOf("Light", "Dark", "System default")
            val currentIndex = when (currentTheme) {
                PreferenceManager.THEME_LIGHT -> 0
                PreferenceManager.THEME_DARK -> 1
                else -> 2
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choose Theme")
                .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                    val theme = when (which) {
                        0 -> PreferenceManager.THEME_LIGHT
                        1 -> PreferenceManager.THEME_DARK
                        else -> PreferenceManager.THEME_SYSTEM
                    }
                    PreferenceManager.setTheme(requireContext(), theme)
                    AppCompatDelegate.setDefaultNightMode(
                        when (theme) {
                            PreferenceManager.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                            PreferenceManager.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        }
                    )
                    binding.tvCurrentTheme.text = options[which]
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun setupSleepTimer() {
        val minutes = PreferenceManager.getSleepTimer(requireContext())
        binding.tvSleepTimer.text = if (minutes == 0) "Off" else "$minutes min"

        binding.rowSleepTimer.setOnClickListener {
            val options = arrayOf("Off", "5 min", "10 min", "15 min", "30 min", "45 min", "60 min")
            val values = intArrayOf(0, 5, 10, 15, 30, 45, 60)
            val currentValue = PreferenceManager.getSleepTimer(requireContext())
            val currentIndex = values.indexOfFirst { it == currentValue }.coerceAtLeast(0)

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sleep Timer")
                .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                    val selected = values[which]
                    PreferenceManager.setSleepTimer(requireContext(), selected)
                    binding.tvSleepTimer.text = options[which]
                    if (selected > 0) {
                        requireContext().showToast("Sleep timer set for ${options[which]}")
                        // TODO: start countdown
                    }
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun setupAbout() {
        binding.rowAbout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Music Player")
                .setMessage(
                    "Version 1.0\n\n" +
                    "A feature-rich music player for Android.\n\n" +
                    "Built with:\n" +
                    "• Media3 / ExoPlayer\n" +
                    "• Room Database\n" +
                    "• Glide\n" +
                    "• Material Design 3"
                )
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
