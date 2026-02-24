package com.morninglock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.provider.Settings
import com.morninglock.R
import com.morninglock.data.LockPreferences
import com.morninglock.data.LockState
import com.morninglock.overlay.LockOverlayManager
import com.morninglock.receiver.ScreenUnlockReceiver

class LockService : Service() {

    companion object {
        const val ACTION_SHOW_OVERLAY = "com.morninglock.ACTION_SHOW_OVERLAY"
        private const val CHANNEL_ID = "morning_lock_service"
        private const val NOTIFICATION_ID = 1
    }

    private var unlockReceiver: ScreenUnlockReceiver? = null
    private var overlayManager: LockOverlayManager? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        registerUnlockReceiver()

        // 服务启动时检查是否有未完成的锁定
        checkPendingLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_OVERLAY -> showLockOverlay()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterUnlockReceiver()
        overlayManager?.removeOverlay()
    }

    private fun registerUnlockReceiver() {
        unlockReceiver = ScreenUnlockReceiver()
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(unlockReceiver, filter)
    }

    private fun unregisterUnlockReceiver() {
        unlockReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {}
        }
        unlockReceiver = null
    }

    private fun checkPendingLock() {
        val prefs = LockPreferences(this)
        val now = System.currentTimeMillis()
        if (LockState.isLockActive(prefs.lockStartTimestamp, prefs.lockDurationMillis, now)) {
            showLockOverlay()
        }
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
        }

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
