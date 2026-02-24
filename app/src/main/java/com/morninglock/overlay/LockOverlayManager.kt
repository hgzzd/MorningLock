package com.morninglock.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.View
import android.widget.TextView
import com.morninglock.R

class LockOverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private var countdownTimer: CountDownTimer? = null
    private var onLockFinished: (() -> Unit)? = null

    fun setOnLockFinishedListener(listener: () -> Unit) {
        onLockFinished = listener
    }

    fun showOverlay(remainingMillis: Long) {
        if (overlayView != null) return // 已经在显示

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        )

        // 拦截所有触摸事件，阻止用户操作底层应用
        params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL.inv()

        val inflater = LayoutInflater.from(context)
        overlayView = inflater.inflate(R.layout.overlay_lock, null)

        windowManager.addView(overlayView, params)
        startCountdown(remainingMillis)
    }

    private fun startCountdown(millis: Long) {
        val tvCountdown = overlayView?.findViewById<TextView>(R.id.tv_countdown) ?: return

        countdownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvCountdown.text = CountdownFormatter.format(millisUntilFinished)
            }

            override fun onFinish() {
                tvCountdown.text = CountdownFormatter.format(0)
                removeOverlay()
                onLockFinished?.invoke()
            }
        }.start()
    }

    fun removeOverlay() {
        countdownTimer?.cancel()
        countdownTimer = null
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
                // View 可能已被移除
            }
        }
        overlayView = null
    }

    fun isShowing(): Boolean = overlayView != null
}
