package com.morninglock.data

import org.junit.Assert.*
import org.junit.Test

class LockStateTest {

    companion object {
        const val LOCK_DURATION_MS = 30 * 60 * 1000L // 30分钟
    }

    // --- shouldTriggerLock ---

    @Test
    fun `未触发过且在时段内应触发锁定`() {
        val result = LockState.shouldTriggerLock(
            lastTriggeredTimestamp = 0L,
            currentTimeMillis = now(),
            currentHour = 7, currentMinute = 0,
            startHour = 6, startMinute = 0,
            endHour = 10, endMinute = 0
        )
        assertTrue(result)
    }

    @Test
    fun `今天已触发过不应再次触发`() {
        val currentTime = now()
        val result = LockState.shouldTriggerLock(
            lastTriggeredTimestamp = currentTime - 60_000, // 1分钟前触发过
            currentTimeMillis = currentTime,
            currentHour = 7, currentMinute = 30,
            startHour = 6, startMinute = 0,
            endHour = 10, endMinute = 0
        )
        assertFalse(result)
    }

    @Test
    fun `昨天触发过今天应重新触发`() {
        val currentTime = now()
        val yesterday = currentTime - 24 * 60 * 60 * 1000L
        val result = LockState.shouldTriggerLock(
            lastTriggeredTimestamp = yesterday,
            currentTimeMillis = currentTime,
            currentHour = 7, currentMinute = 0,
            startHour = 6, startMinute = 0,
            endHour = 10, endMinute = 0
        )
        assertTrue(result)
    }

    @Test
    fun `不在生效时段内不应触发`() {
        val result = LockState.shouldTriggerLock(
            lastTriggeredTimestamp = 0L,
            currentTimeMillis = now(),
            currentHour = 12, currentMinute = 0,
            startHour = 6, startMinute = 0,
            endHour = 10, endMinute = 0
        )
        assertFalse(result)
    }

    @Test
    fun `锁定进行中不应再次触发`() {
        val currentTime = now()
        val lockStart = currentTime - 5 * 60 * 1000L
        val result = LockState.shouldTriggerLockWithActiveCheck(
            lastTriggeredTimestamp = 0L,
            lockStartTimestamp = lockStart,
            lockDurationMillis = LOCK_DURATION_MS,
            currentTimeMillis = currentTime,
            currentHour = 7, currentMinute = 0,
            startHour = 6, startMinute = 0,
            endHour = 10, endMinute = 0
        )
        assertFalse(result)
    }

    @Test
    fun `无进行中锁定且今天未触发应触发`() {
        val currentTime = now()
        val result = LockState.shouldTriggerLockWithActiveCheck(
            lastTriggeredTimestamp = 0L,
            lockStartTimestamp = 0L,
            lockDurationMillis = LOCK_DURATION_MS,
            currentTimeMillis = currentTime,
            currentHour = 7, currentMinute = 0,
            startHour = 6, startMinute = 0,
            endHour = 10, endMinute = 0
        )
        assertTrue(result)
    }

    @Test
    fun `结束时段时锁定进行中不应停止服务`() {
        val currentTime = now()
        val lockStart = currentTime - 5 * 60 * 1000L
        val shouldStop = LockState.shouldStopServiceForWindowEnd(
            lockStartTimestamp = lockStart,
            durationMillis = LOCK_DURATION_MS,
            currentTimeMillis = currentTime
        )
        assertFalse(shouldStop)
    }

    @Test
    fun `结束时段时无进行中锁定应停止服务`() {
        val currentTime = now()
        val shouldStop = LockState.shouldStopServiceForWindowEnd(
            lockStartTimestamp = 0L,
            durationMillis = LOCK_DURATION_MS,
            currentTimeMillis = currentTime
        )
        assertTrue(shouldStop)
    }

    @Test
    fun `结束时段时锁定已到期应停止服务`() {
        val currentTime = now()
        val lockStart = currentTime - LOCK_DURATION_MS
        val shouldStop = LockState.shouldStopServiceForWindowEnd(
            lockStartTimestamp = lockStart,
            durationMillis = LOCK_DURATION_MS,
            currentTimeMillis = currentTime
        )
        assertTrue(shouldStop)
    }

    // --- isLockActive ---

    @Test
    fun `锁定中未超时应返回active`() {
        val lockStart = now() - 10 * 60 * 1000L // 10分钟前开始锁定
        assertTrue(LockState.isLockActive(lockStart, LOCK_DURATION_MS, now()))
    }

    @Test
    fun `锁定已超时应返回inactive`() {
        val lockStart = now() - 31 * 60 * 1000L // 31分钟前开始锁定
        assertFalse(LockState.isLockActive(lockStart, LOCK_DURATION_MS, now()))
    }

    @Test
    fun `锁定恰好到期应返回inactive`() {
        val currentTime = now()
        val lockStart = currentTime - LOCK_DURATION_MS
        assertFalse(LockState.isLockActive(lockStart, LOCK_DURATION_MS, currentTime))
    }

    @Test
    fun `未开始锁定应返回inactive`() {
        assertFalse(LockState.isLockActive(0L, LOCK_DURATION_MS, now()))
    }

    // --- getRemainingMillis ---

    @Test
    fun `剩余时间计算正确`() {
        val currentTime = now()
        val lockStart = currentTime - 10 * 60 * 1000L
        val remaining = LockState.getRemainingMillis(lockStart, LOCK_DURATION_MS, currentTime)
        assertEquals(20 * 60 * 1000L, remaining)
    }

    @Test
    fun `已超时剩余时间为0`() {
        val currentTime = now()
        val lockStart = currentTime - 31 * 60 * 1000L
        val remaining = LockState.getRemainingMillis(lockStart, LOCK_DURATION_MS, currentTime)
        assertEquals(0L, remaining)
    }

    private fun now(): Long = System.currentTimeMillis()
}
