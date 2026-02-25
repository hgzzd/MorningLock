package com.morninglock

import android.Manifest
import android.app.TimePickerDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.morninglock.data.LockPreferences
import com.morninglock.service.LockService
import com.morninglock.util.AlarmScheduler
import com.morninglock.util.TimeUtils
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: LockPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = LockPreferences(this)

        setupServiceSwitch()
        setupTimePickers()
        setupDurationSetting()
        setupPermissionButtons()
        setupMiuiButtons()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionButtonStates()
        ensureServiceRunningIfNeeded()
    }

    /**
     * 确保服务在需要时运行。
     * 如果服务已启用且当前在生效时段内，启动 LockService。
     */
    private fun ensureServiceRunningIfNeeded() {
        if (!prefs.serviceEnabled) return
        if (!Settings.canDrawOverlays(this)) return

        val cal = Calendar.getInstance()
        val isInPeriod = TimeUtils.isInTimePeriod(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            prefs.startHour,
            prefs.startMinute,
            prefs.endHour,
            prefs.endMinute
        )

        if (isInPeriod) {
            // 确保闹钟已设置
            AlarmScheduler.scheduleDaily(this, prefs)
        }
    }

    // --- 服务开关 ---

    private fun setupServiceSwitch() {
        val switch = findViewById<SwitchMaterial>(R.id.switch_service)
        switch.isChecked = prefs.serviceEnabled

        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.serviceEnabled = isChecked
            if (isChecked) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show()
                    switch.isChecked = false
                    prefs.serviceEnabled = false
                    return@setOnCheckedChangeListener
                }
                AlarmScheduler.scheduleDaily(this, prefs)
            } else {
                AlarmScheduler.cancelAll(this)
                stopService(Intent(this, LockService::class.java))
            }
        }
    }

    // --- 时段选择 ---

    private fun setupTimePickers() {
        val btnStart = findViewById<Button>(R.id.btn_start_time)
        val btnEnd = findViewById<Button>(R.id.btn_end_time)

        btnStart.text = formatTime(prefs.startHour, prefs.startMinute)
        btnEnd.text = formatTime(prefs.endHour, prefs.endMinute)

        btnStart.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                prefs.startHour = hour
                prefs.startMinute = minute
                btnStart.text = formatTime(hour, minute)
                rescheduleIfEnabled()
            }, prefs.startHour, prefs.startMinute, true).show()
        }

        btnEnd.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                prefs.endHour = hour
                prefs.endMinute = minute
                btnEnd.text = formatTime(hour, minute)
                rescheduleIfEnabled()
            }, prefs.endHour, prefs.endMinute, true).show()
        }
    }

    private fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

    private fun rescheduleIfEnabled() {
        if (prefs.serviceEnabled) {
            AlarmScheduler.scheduleDaily(this, prefs)
        }
    }

    // --- 锁定时长 ---

    private val durationOptions = listOf(
        15 to "15 分钟",
        30 to "30 分钟",
        60 to "1 小时",
        120 to "2 小时",
        -1 to "自定义..."
    )

    private fun setupDurationSetting() {
        val btnDuration = findViewById<Button>(R.id.btn_duration)
        btnDuration.text = formatDuration(prefs.lockDurationMinutes)

        btnDuration.setOnClickListener {
            val labels = durationOptions.map { it.second }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("选择锁定时长")
                .setItems(labels) { _, which ->
                    val minutes = durationOptions[which].first
                    if (minutes == -1) {
                        showCustomDurationDialog(btnDuration)
                    } else {
                        prefs.lockDurationMinutes = minutes
                        btnDuration.text = formatDuration(minutes)
                    }
                }
                .show()
        }
    }

    private fun showCustomDurationDialog(btnDuration: Button) {
        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "输入分钟数"
            setPadding(60, 40, 60, 40)
        }
        AlertDialog.Builder(this)
            .setTitle("自定义锁定时长（分钟）")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val value = input.text.toString().toIntOrNull()
                if (value != null && value > 0) {
                    prefs.lockDurationMinutes = value
                    btnDuration.text = formatDuration(value)
                } else {
                    Toast.makeText(this, "请输入有效的分钟数", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun formatDuration(minutes: Int): String {
        return when {
            minutes >= 60 && minutes % 60 == 0 -> "${minutes / 60} 小时"
            minutes > 60 -> "${minutes / 60} 小时 ${minutes % 60} 分钟"
            else -> "$minutes 分钟"
        }
    }

    // --- 权限 ---

    private fun setupPermissionButtons() {
        findViewById<Button>(R.id.btn_overlay_permission).setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_notification_permission).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }

    private fun updatePermissionButtonStates() {
        val btnOverlay = findViewById<Button>(R.id.btn_overlay_permission)
        val btnNotification = findViewById<Button>(R.id.btn_notification_permission)

        if (Settings.canDrawOverlays(this)) {
            btnOverlay.text = "悬浮窗权限 ✓"
            btnOverlay.isEnabled = false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                btnNotification.text = "通知权限 ✓"
                btnNotification.isEnabled = false
            }
        } else {
            btnNotification.text = "通知权限 ✓"
            btnNotification.isEnabled = false
        }
    }

    // --- MIUI 特殊设置 ---

    private fun setupMiuiButtons() {
        findViewById<Button>(R.id.btn_miui_autostart).setOnClickListener {
            tryOpenMiuiAutoStart()
        }

        findViewById<Button>(R.id.btn_miui_battery).setOnClickListener {
            tryOpenBatteryOptimization()
        }

        findViewById<Button>(R.id.btn_miui_background).setOnClickListener {
            tryOpenMiuiPermissions()
        }
    }

    private fun tryOpenMiuiAutoStart() {
        try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            }
            startActivity(intent)
        } catch (_: Exception) {
            // 非 MIUI 系统，打开应用详情页
            openAppSettings()
        }
    }

    private fun tryOpenBatteryOptimization() {
        try {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "已关闭电池优化", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            openAppSettings()
        }
    }

    private fun tryOpenMiuiPermissions() {
        try {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
                )
                putExtra("extra_pkgname", packageName)
            }
            startActivity(intent)
        } catch (_: Exception) {
            openAppSettings()
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }
}
