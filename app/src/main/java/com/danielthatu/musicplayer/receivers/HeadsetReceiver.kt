package com.danielthatu.musicplayer.receivers

import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.danielthatu.musicplayer.services.MusicService

class HeadsetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_HEADSET_PLUG -> {
                val state = intent.getIntExtra("state", -1)
                if (state == 0) {
                    // Headset unplugged - pause playback
                    val pauseIntent = Intent(context, MusicService::class.java).apply {
                        action = MusicService.ACTION_PAUSE
                    }
                    context.startService(pauseIntent)
                }
            }
            BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(
                    BluetoothHeadset.EXTRA_STATE,
                    BluetoothHeadset.STATE_DISCONNECTED
                )
                if (state == BluetoothHeadset.STATE_DISCONNECTED) {
                    val pauseIntent = Intent(context, MusicService::class.java).apply {
                        action = MusicService.ACTION_PAUSE
                    }
                    context.startService(pauseIntent)
                }
            }
        }
    }
}
