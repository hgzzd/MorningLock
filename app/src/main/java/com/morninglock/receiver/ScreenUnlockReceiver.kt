package com.morninglock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.morninglock.data.LockPreferences
import com.morninglock.service.LockService

class ScreenUnlockReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScreenUnlockReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_USER_PRESENT) return
        Log.i(TAG, "Received ACTION_USER_PRESENT")

        val prefs = LockPreferences(context)

        // 检查服务是否已启用
        if (!prefs.serviceEnabled) return

        val serviceIntent = Intent(context, LockService::class.java).apply {
            action = LockService.ACTION_EVALUATE_TRIGGER
            putExtra(LockService.EXTRA_TRIGGER_REASON, "unlock")
        }
        context.startForegroundService(serviceIntent)
    }
}
