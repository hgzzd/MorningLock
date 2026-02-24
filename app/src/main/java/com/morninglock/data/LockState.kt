package com.morninglock.data

import com.morninglock.util.TimeUtils

object LockState {

    /**
     * 判断是否应该触发锁定。
     * 条件：在生效时段内 且 今天尚未触发过。
     */
    fun shouldTriggerLock(
        lastTriggeredTimestamp: Long,
        currentTimeMillis: Long,
        currentHour: Int, currentMinute: Int,
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int
    ): Boolean {
        if (!TimeUtils.isInTimePeriod(currentHour, currentMinute, startHour, startMinute, endHour, endMinute)) {
            return false
        }
        if (TimeUtils.isSameDay(lastTriggeredTimestamp, currentTimeMillis)) {
            return false
        }
        return true
    }

    /**
     * 判断锁定是否仍在生效中。
     * lockStartTimestamp 为 0 表示未开始锁定。
     */
    fun isLockActive(lockStartTimestamp: Long, durationMillis: Long, currentTimeMillis: Long): Boolean {
        if (lockStartTimestamp == 0L) return false
        return currentTimeMillis < lockStartTimestamp + durationMillis
    }

    /**
     * 获取剩余锁定毫秒数，最小为 0。
     */
    fun getRemainingMillis(lockStartTimestamp: Long, durationMillis: Long, currentTimeMillis: Long): Long {
        val remaining = lockStartTimestamp + durationMillis - currentTimeMillis
        return maxOf(0L, remaining)
    }
}
