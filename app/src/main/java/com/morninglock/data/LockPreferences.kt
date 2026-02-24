package com.morninglock.data

import android.content.Context
import android.content.SharedPreferences

class LockPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("morning_lock_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_START_HOUR = "start_hour"
        private const val KEY_START_MINUTE = "start_minute"
        private const val KEY_END_HOUR = "end_hour"
        private const val KEY_END_MINUTE = "end_minute"
        private const val KEY_LAST_TRIGGERED = "last_triggered"
        private const val KEY_LOCK_START = "lock_start"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_LOCK_DURATION_MINUTES = "lock_duration_minutes"

        const val DEFAULT_START_HOUR = 5
        const val DEFAULT_START_MINUTE = 0
        const val DEFAULT_END_HOUR = 10
        const val DEFAULT_END_MINUTE = 0
        const val DEFAULT_LOCK_DURATION_MINUTES = 30
    }

    var startHour: Int
        get() = prefs.getInt(KEY_START_HOUR, DEFAULT_START_HOUR)
        set(value) = prefs.edit().putInt(KEY_START_HOUR, value).apply()

    var startMinute: Int
        get() = prefs.getInt(KEY_START_MINUTE, DEFAULT_START_MINUTE)
        set(value) = prefs.edit().putInt(KEY_START_MINUTE, value).apply()

    var endHour: Int
        get() = prefs.getInt(KEY_END_HOUR, DEFAULT_END_HOUR)
        set(value) = prefs.edit().putInt(KEY_END_HOUR, value).apply()

    var endMinute: Int
        get() = prefs.getInt(KEY_END_MINUTE, DEFAULT_END_MINUTE)
        set(value) = prefs.edit().putInt(KEY_END_MINUTE, value).apply()

    var lastTriggeredTimestamp: Long
        get() = prefs.getLong(KEY_LAST_TRIGGERED, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_TRIGGERED, value).apply()

    var lockStartTimestamp: Long
        get() = prefs.getLong(KEY_LOCK_START, 0L)
        set(value) = prefs.edit().putLong(KEY_LOCK_START, value).apply()

    var serviceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()

    var lockDurationMinutes: Int
        get() = prefs.getInt(KEY_LOCK_DURATION_MINUTES, DEFAULT_LOCK_DURATION_MINUTES)
        set(value) = prefs.edit().putInt(KEY_LOCK_DURATION_MINUTES, value).apply()

    val lockDurationMillis: Long
        get() = lockDurationMinutes * 60 * 1000L
}
