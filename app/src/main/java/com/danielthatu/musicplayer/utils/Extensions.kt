package com.danielthatu.musicplayer.utils

import android.content.Context
import android.widget.Toast
import java.util.concurrent.TimeUnit

fun Long.toFormattedTime(): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%d:%02d", minutes, seconds)
}

fun Long.toFormattedSize(): String {
    return when {
        this < 1024 -> "$this B"
        this < 1024 * 1024 -> "${this / 1024} KB"
        else -> String.format("%.1f MB", this / (1024.0 * 1024.0))
    }
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun String.truncate(maxLength: Int): String {
    return if (length <= maxLength) this else substring(0, maxLength - 3) + "..."
}

// Standalone helper for use in fragments without extension receiver
object Extensions {
    fun toFormattedSize(size: Long): String = size.toFormattedSize()
}
