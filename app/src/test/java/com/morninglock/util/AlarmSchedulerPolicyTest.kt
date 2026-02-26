package com.morninglock.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmSchedulerPolicyTest {

    @Test
    fun `android12及以上且无精确闹钟能力时不使用exact`() {
        val shouldUseExact = AlarmScheduler.shouldUseExactAlarm(
            sdkInt = 31,
            canScheduleExactAlarms = false
        )
        assertFalse(shouldUseExact)
    }

    @Test
    fun `android12及以上且有精确闹钟能力时使用exact`() {
        val shouldUseExact = AlarmScheduler.shouldUseExactAlarm(
            sdkInt = 31,
            canScheduleExactAlarms = true
        )
        assertTrue(shouldUseExact)
    }

    @Test
    fun `android11及以下始终使用exact`() {
        val shouldUseExact = AlarmScheduler.shouldUseExactAlarm(
            sdkInt = 30,
            canScheduleExactAlarms = false
        )
        assertTrue(shouldUseExact)
    }
}
