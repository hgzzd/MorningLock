package com.morninglock.overlay

import org.junit.Assert.*
import org.junit.Test

class CountdownFormatterTest {

    @Test
    fun `30分钟格式化为30_00`() {
        assertEquals("30:00", CountdownFormatter.format(30 * 60 * 1000L))
    }

    @Test
    fun `1分1秒格式化为01_01`() {
        assertEquals("01:01", CountdownFormatter.format(61 * 1000L))
    }

    @Test
    fun `0毫秒格式化为00_00`() {
        assertEquals("00:00", CountdownFormatter.format(0L))
    }

    @Test
    fun `59秒格式化为00_59`() {
        assertEquals("00:59", CountdownFormatter.format(59 * 1000L))
    }

    @Test
    fun `10分30秒格式化为10_30`() {
        assertEquals("10:30", CountdownFormatter.format(10 * 60 * 1000L + 30 * 1000L))
    }

    @Test
    fun `负数返回00_00`() {
        assertEquals("00:00", CountdownFormatter.format(-1000L))
    }

    @Test
    fun `带余数毫秒向下取整`() {
        // 61999ms = 1分01秒999毫秒，显示 01:01
        assertEquals("01:01", CountdownFormatter.format(61_999L))
    }
}
