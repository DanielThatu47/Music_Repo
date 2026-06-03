package com.danielthatu.musicplayer.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {

    private const val PREF_NAME = "music_player_prefs"
    private const val KEY_LAST_SONG_ID = "last_song_id"
    private const val KEY_LAST_POSITION = "last_position"
    private const val KEY_REPEAT_MODE = "repeat_mode"
    private const val KEY_SHUFFLE = "shuffle_enabled"
    private const val KEY_THEME = "theme"
    private const val KEY_SORT_ORDER = "sort_order"
    private const val KEY_SLEEP_TIMER = "sleep_timer_minutes"
    private const val KEY_EQUALIZER_ENABLED = "equalizer_enabled"

    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"

    const val SORT_BY_TITLE = "title"
    const val SORT_BY_ARTIST = "artist"
    const val SORT_BY_ALBUM = "album"
    const val SORT_BY_DATE = "date"
    const val SORT_BY_DURATION = "duration"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getLastSongId(context: Context): Long = prefs(context).getLong(KEY_LAST_SONG_ID, -1L)
    fun setLastSongId(context: Context, id: Long) = prefs(context).edit().putLong(KEY_LAST_SONG_ID, id).apply()

    fun getLastPosition(context: Context): Long = prefs(context).getLong(KEY_LAST_POSITION, 0L)
    fun setLastPosition(context: Context, pos: Long) = prefs(context).edit().putLong(KEY_LAST_POSITION, pos).apply()

    fun getRepeatMode(context: Context): Int = prefs(context).getInt(KEY_REPEAT_MODE, 0)
    fun setRepeatMode(context: Context, mode: Int) = prefs(context).edit().putInt(KEY_REPEAT_MODE, mode).apply()

    fun isShuffleEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_SHUFFLE, false)
    fun setShuffleEnabled(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_SHUFFLE, enabled).apply()

    fun getTheme(context: Context): String = prefs(context).getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    fun setTheme(context: Context, theme: String) = prefs(context).edit().putString(KEY_THEME, theme).apply()

    fun getSortOrder(context: Context): String = prefs(context).getString(KEY_SORT_ORDER, SORT_BY_TITLE) ?: SORT_BY_TITLE
    fun setSortOrder(context: Context, order: String) = prefs(context).edit().putString(KEY_SORT_ORDER, order).apply()

    fun getSleepTimer(context: Context): Int = prefs(context).getInt(KEY_SLEEP_TIMER, 0)
    fun setSleepTimer(context: Context, minutes: Int) = prefs(context).edit().putInt(KEY_SLEEP_TIMER, minutes).apply()

    fun isEqualizerEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_EQUALIZER_ENABLED, false)
    fun setEqualizerEnabled(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_EQUALIZER_ENABLED, enabled).apply()
}
