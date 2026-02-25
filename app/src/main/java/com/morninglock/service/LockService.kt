package com.morninglock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import com.morninglock.R
import com.morninglock.data.LockPreferences
import com.morninglock.data.LockState
import com.morninglock.overlay.LockOverlayManager
import com.morninglock.receiver.ScreenUnlockReceiver
import com.morninglock.util.TimeUtils

class LockService : Service() {

    companion object {
        const val ACTION_SHOW_OVERLAY = "com.morninglock.ACTION_SHOW_OVERLAY"
        const val ACTION_EVALUATE_TRIGGER = "com.morninglock.ACTION_EVALUATE_TRIGGER"
        const val EXTRA_TRIGGER_REASON = "extra_trigger_reason"
        private const val CHANNEL_ID = "morning_lock_service"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "LockService"
    }

    private var unlockReceiver: ScreenUnlockReceiver? = null
    private var overlayManager: LockOverlayManager? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service onCreate")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        registerUnlockReceiver()

        // 服务启动时检查是否有未完成的锁定
        checkPendingLock()
        // 兜底：服务启动时在时段内且当天未触发，立即触发一次
        evaluateAndTriggerLockIfNeeded("service_create")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_OVERLAY -> showLockOverlay()
            ACTION_EVALUATE_TRIGGER -> {
                val reason = intent.getStringExtra(EXTRA_TRIGGER_REASON) ?: "unknown"
                evaluateAndTriggerLockIfNeeded(reason)
            }
        }
        Log.i(TAG, "onStartCommand action=${intent?.action}")
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Service onDestroy")
        unregisterUnlockReceiver()
        overlayManager?.removeOverlay()
    }

    private fun registerUnlockReceiver() {
        unlockReceiver = ScreenUnlockReceiver()
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(unlockReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(unlockReceiver, filter)
            }
            Log.i(TAG, "Unlock receiver registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register unlock receiver", e)
        }
    }

    private fun unregisterUnlockReceiver() {
        unlockReceiver?.let {
            try {
                unregisterReceiver(it)
                Log.i(TAG, "Unlock receiver unregistered")
            } catch (_: Exception) {}
        }
        unlockReceiver = null
    }

    private fun checkPendingLock() {
        val prefs = LockPreferences(this)
        val now = System.currentTimeMillis()
        if (LockState.isLockActive(prefs.lockStartTimestamp, prefs.lockDurationMillis, now)) {
            Log.i(TAG, "Pending lock found, showing overlay")
            showLockOverlay()
        }
    }

    private fun evaluateAndTriggerLockIfNeeded(reason: String) {
        val prefs = LockPreferences(this)
        if (!prefs.serviceEnabled) {
            Log.i(TAG, "Skip trigger, service disabled, reason=$reason")
            return
        }

        val now = System.currentTimeMillis()
        val cal = java.util.Calendar.getInstance()
        val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(java.util.Calendar.MINUTE)

        if (!TimeUtils.isInTimePeriod(
                currentHour,
                currentMinute,
                prefs.startHour,
                prefs.startMinute,
                prefs.endHour,
                prefs.endMinute
            )
        ) {
            Log.i(TAG, "Skip trigger, outside period, reason=$reason")
            return
        }
        if (LockState.isLockActive(prefs.lockStartTimestamp, prefs.lockDurationMillis, now)) {
            Log.i(TAG, "Skip trigger, lock is active, reason=$reason")
            return
        }
        if (TimeUtils.isSameDay(prefs.lastTriggeredTimestamp, now)) {
            Log.i(TAG, "Skip trigger, already triggered today, reason=$reason")
            return
        }

        prefs.lastTriggeredTimestamp = now
        prefs.lockStartTimestamp = now
        Log.i(TAG, "Trigger lock, reason=$reason, start=$now")
        showLockOverlay()
    }

    private fun showLockOverlay() {
        if (!Settings.canDrawOverlays(this)) return

        val prefs = LockPreferences(this)
        val now = System.currentTimeMillis()
        val remaining = LockState.getRemainingMillis(prefs.lockStartTimestamp, prefs.lockDurationMillis, now)

        if (remaining <= 0) return

        if (overlayManager == null) {
            overlayManager = LockOverlayManager(this)
        }

        overlayManager?.setOnLockFinishedListener {
            // 锁定结束，清理状态
            prefs.lockStartTimestamp = 0L
            Log.i(TAG, "Lock finished, clear lockStartTimestamp")
        }

        Log.i(TAG, "Show overlay, remaining=${remaining}ms")
        overlayManager?.showOverlay(remaining)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_desc)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.service_running))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .build()
    }
}
