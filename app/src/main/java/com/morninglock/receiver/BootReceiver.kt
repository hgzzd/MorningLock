package com.morninglock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.morninglock.data.LockPreferences
import com.morninglock.util.AlarmScheduler

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = LockPreferences(context)
        if (prefs.serviceEnabled) {
            // 重新注册闹钟，如果当前在生效时段内会立即启动服务
            AlarmScheduler.scheduleDaily(context, prefs)
        }
    }
}
