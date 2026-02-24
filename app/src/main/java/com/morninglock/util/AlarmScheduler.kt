package com.morninglock.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.morninglock.data.LockPreferences
import com.morninglock.receiver.AlarmReceiver
import com.morninglock.service.LockService
import java.util.Calendar

object AlarmScheduler {

    private const val REQUEST_START = 1001
    private const val REQUEST_STOP = 1002

    /**
     * 注册每日启动/停止服务的闹钟。
     * 如果当前已在生效时段内，立即启动服务。
     */
    fun scheduleDaily(context: Context, prefs: LockPreferences) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 注册启动闹钟（生效开始时间）
        val startTime = getNextAlarmTime(prefs.startHour, prefs.startMinute)
        val startIntent = createPendingIntent(context, AlarmReceiver.ACTION_START_SERVICE, REQUEST_START)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, startIntent)

        // 注册停止闹钟（生效结束时间）
        val stopTime = getNextAlarmTime(prefs.endHour, prefs.endMinute)
        val stopIntent = createPendingIntent(context, AlarmReceiver.ACTION_STOP_SERVICE, REQUEST_STOP)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, stopTime, stopIntent)

        // 如果当前已在生效时段内，立即启动服务
        if (TimeUtils.isInTimePeriod(
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                prefs.startHour, prefs.startMinute,
                prefs.endHour, prefs.endMinute
            )
        ) {
            context.startForegroundService(Intent(context, LockService::class.java))
        }
    }

    /**
     * 仅重新注册启动闹钟（用于 AlarmReceiver 触发后注册下一天的）
     */
    fun rescheduleStart(context: Context, prefs: LockPreferences) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val startTime = getNextAlarmTime(prefs.startHour, prefs.startMinute)
        val startIntent = createPendingIntent(context, AlarmReceiver.ACTION_START_SERVICE, REQUEST_START)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, startIntent)
    }

    /**
     * 仅重新注册停止闹钟
     */
    fun rescheduleStop(context: Context, prefs: LockPreferences) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val stopTime = getNextAlarmTime(prefs.endHour, prefs.endMinute)
        val stopIntent = createPendingIntent(context, AlarmReceiver.ACTION_STOP_SERVICE, REQUEST_STOP)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, stopTime, stopIntent)
    }

    /**
     * 取消所有闹钟
     */
    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(createPendingIntent(context, AlarmReceiver.ACTION_START_SERVICE, REQUEST_START))
        alarmManager.cancel(createPendingIntent(context, AlarmReceiver.ACTION_STOP_SERVICE, REQUEST_STOP))
    }

    /**
     * 计算下一次触发时间。
     * 如果今天的目标时间已过，则设为明天。
     */
    private fun getNextAlarmTime(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun createPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
