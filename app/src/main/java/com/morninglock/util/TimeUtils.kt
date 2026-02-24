package com.morninglock.util

import java.util.Calendar

object TimeUtils {

    /**
     * 判断当前时间是否在指定时段内。
     * 支持跨午夜时段（如 23:00-06:00）。
     * 开始时间包含，结束时间不包含。
     * 开始等于结束视为全天生效。
     */
    fun isInTimePeriod(
        currentHour: Int, currentMinute: Int,
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int
    ): Boolean {
        val current = currentHour * 60 + currentMinute
        val start = startHour * 60 + startMinute
        val end = endHour * 60 + endMinute

        if (start == end) return true // 全天生效

        return if (start < end) {
            current >= start && current < end
        } else {
            // 跨午夜
            current >= start || current < end
        }
    }

    /**
     * 判断两个时间戳是否是同一天。
     * 任一时间戳为0则返回false。
     */
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        if (timestamp1 == 0L || timestamp2 == 0L) return false
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
