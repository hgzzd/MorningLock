package com.morninglock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenUnlockReceiver(private val onUnlock: (() -> Unit)? = null) : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScreenUnlockReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_USER_PRESENT) return
        Log.i(TAG, "Received ACTION_USER_PRESENT, invoking callback")
        onUnlock?.invoke()
    }
}
