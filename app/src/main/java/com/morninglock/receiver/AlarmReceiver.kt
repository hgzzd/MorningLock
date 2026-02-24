package com.morninglock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.morninglock.data.LockPreferences
import com.morninglock.service.LockService
import com.morninglock.util.AlarmScheduler

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_START_SERVICE = "com.morninglock.ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.morninglock.ACTION_STOP_SERVICE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = LockPreferences(context)
        if (!prefs.serviceEnabled) return

        when (intent.action) {
            ACTION_START_SERVICE -> {
                context.startForegroundService(Intent(context, LockService::class.java))
                // 注册明天的启动闹钟
                AlarmScheduler.rescheduleStart(context, prefs)
            }
            ACTION_STOP_SERVICE -> {
                context.stopService(Intent(context, LockService::class.java))
                // 注册明天的停止闹钟
                AlarmScheduler.rescheduleStop(context, prefs)
            }
        }
    }
}
