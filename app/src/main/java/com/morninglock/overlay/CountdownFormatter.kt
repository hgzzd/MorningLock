package com.morninglock.overlay

object CountdownFormatter {

    /**
     * 将毫秒数格式化为 "MM:SS" 格式。
     * 负数返回 "00:00"。
     */
    fun format(millis: Long): String {
        if (millis <= 0) return "00:00"
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
