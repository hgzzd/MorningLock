package com.morninglock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.morninglock.data.LockPreferences
import com.morninglock.data.LockState
import com.morninglock.service.LockService
import java.util.Calendar

class ScreenUnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_USER_PRESENT) return

        val prefs = LockPreferences(context)
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        val shouldTrigger = LockState.shouldTriggerLock(
            lastTriggeredTimestamp = prefs.lastTriggeredTimestamp,
            currentTimeMillis = now,
            currentHour = cal.get(Calendar.HOUR_OF_DAY),
            currentMinute = cal.get(Calendar.MINUTE),
            startHour = prefs.startHour,
            startMinute = prefs.startMinute,
            endHour = prefs.endHour,
            endMinute = prefs.endMinute
        )

        if (shouldTrigger) {
            prefs.lastTriggeredTimestamp = now
            prefs.lockStartTimestamp = now

            val serviceIntent = Intent(context, LockService::class.java).apply {
                action = LockService.ACTION_SHOW_OVERLAY
            }
            context.startForegroundService(serviceIntent)
        }
    }
}
