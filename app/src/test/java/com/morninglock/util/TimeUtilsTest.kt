package com.morninglock.util

import org.junit.Assert.*
import org.junit.Test

class TimeUtilsTest {

    // --- isInTimePeriod 基本场景 ---

    @Test
    fun `时间在时段内应返回true`() {
        // 时段 6:00 - 9:00，当前 7:30
        assertTrue(TimeUtils.isInTimePeriod(7, 30, 6, 0, 9, 0))
    }

    @Test
    fun `时间在时段外应返回false`() {
        // 时段 6:00 - 9:00，当前 10:00
        assertFalse(TimeUtils.isInTimePeriod(10, 0, 6, 0, 9, 0))
    }

    @Test
    fun `时间在时段之前应返回false`() {
        // 时段 6:00 - 9:00，当前 5:00
        assertFalse(TimeUtils.isInTimePeriod(5, 0, 6, 0, 9, 0))
    }

    // --- 边界值 ---

    @Test
    fun `恰好等于开始时间应返回true`() {
        // 时段 6:00 - 9:00，当前 6:00
        assertTrue(TimeUtils.isInTimePeriod(6, 0, 6, 0, 9, 0))
    }

    @Test
    fun `恰好等于结束时间应返回false`() {
        // 时段 6:00 - 9:00，当前 9:00（结束时间不包含）
        assertFalse(TimeUtils.isInTimePeriod(9, 0, 6, 0, 9, 0))
    }

    @Test
    fun `结束时间前一分钟应返回true`() {
        assertTrue(TimeUtils.isInTimePeriod(8, 59, 6, 0, 9, 0))
    }

    // --- 跨午夜时段 ---

    @Test
    fun `跨午夜时段_夜间时间应返回true`() {
        // 时段 23:00 - 6:00，当前 1:00
        assertTrue(TimeUtils.isInTimePeriod(1, 0, 23, 0, 6, 0))
    }

    @Test
    fun `跨午夜时段_晚间时间应返回true`() {
        // 时段 23:00 - 6:00，当前 23:30
        assertTrue(TimeUtils.isInTimePeriod(23, 30, 23, 0, 6, 0))
    }

    @Test
    fun `跨午夜时段_白天时间应返回false`() {
        // 时段 23:00 - 6:00，当前 12:00
        assertFalse(TimeUtils.isInTimePeriod(12, 0, 23, 0, 6, 0))
    }

    @Test
    fun `跨午夜时段_恰好等于开始时间应返回true`() {
        assertTrue(TimeUtils.isInTimePeriod(23, 0, 23, 0, 6, 0))
    }

    @Test
    fun `跨午夜时段_恰好等于结束时间应返回false`() {
        assertFalse(TimeUtils.isInTimePeriod(6, 0, 23, 0, 6, 0))
    }

    // --- 全天时段 ---

    @Test
    fun `开始等于结束时间视为全天生效`() {
        // 0:00 - 0:00 表示全天
        assertTrue(TimeUtils.isInTimePeriod(12, 0, 0, 0, 0, 0))
    }

    // --- isSameDay ---

    @Test
    fun `同一天应返回true`() {
        val time1 = createTimestamp(2024, 1, 15, 8, 0)
        val time2 = createTimestamp(2024, 1, 15, 20, 0)
        assertTrue(TimeUtils.isSameDay(time1, time2))
    }

    @Test
    fun `不同天应返回false`() {
        val time1 = createTimestamp(2024, 1, 15, 23, 59)
        val time2 = createTimestamp(2024, 1, 16, 0, 1)
        assertFalse(TimeUtils.isSameDay(time1, time2))
    }

    @Test
    fun `时间戳为0应返回false`() {
        val now = createTimestamp(2024, 1, 15, 8, 0)
        assertFalse(TimeUtils.isSameDay(0L, now))
    }

    // --- 辅助方法 ---

    private fun createTimestamp(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(year, month - 1, day, hour, minute, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
