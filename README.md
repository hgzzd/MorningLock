# MorningLock 晨起锁机

每天早上醒来第一件事就是拿起手机刷社交媒体、看视频，躺在床上一刷就是半小时甚至一小时。短视频和信息流带来的多巴胺刺激会让大脑在一天的开始就进入"高刺激低回报"模式，导致整天注意力涣散、精神萎靡。

MorningLock 的做法很简单粗暴：检测到你早上第一次解锁手机时，直接锁定屏幕 30 分钟（可自定义）。一个全屏倒计时覆盖层挡住一切操作，没有退出按钮，没有紧急解锁，逼你放下手机去洗漱、吃早餐、出门。

## 功能

- 检测每天第一次解锁，自动触发锁屏
- 全屏不可关闭的倒计时覆盖层
- 自定义生效时段（如 05:00 - 12:00）
- 自定义锁定时长（15分钟 / 30分钟 / 1小时 / 2小时 / 自定义）
- AlarmManager 定时调度，仅在生效时段内运行服务，不常驻后台
- 开机自启，重启手机后自动恢复
- MIUI / HyperOS 权限引导（自启动、电池优化、后台弹出界面）

## 适配

在红米 Note 12 Turbo（Android 13, MIUI 14）上开发和测试。理论上支持 Android 8.0（API 26）及以上设备。

## 构建

```bash
# 需要 JDK 17 + Android SDK (platform 34, build-tools 34.0.0)
./gradlew test           # 运行单元测试
./gradlew assembleDebug  # 构建 debug APK
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 安装后设置

1. 打开 App，授予悬浮窗权限和通知权限
2. 设置生效时段和锁定时长
3. 打开"启动锁机服务"开关
4. （MIUI 用户）点击 App 内的按钮跳转到对应设置页，开启自启动、关闭电池优化、允许后台弹出界面

## 工作原理

```
用户开启服务
  → AlarmManager 注册两个精确闹钟（开始时间 / 结束时间）
  → 到达开始时间 → AlarmReceiver 启动前台服务 → 注册解锁监听
  → 用户解锁手机 → 判断今天是否已触发过
  → 首次解锁 → 显示全屏覆盖层 → 倒计时结束自动消失
  → 到达结束时间 → AlarmReceiver 停止服务 → 释放资源
  → 每天重复
```

## 项目结构

```
app/src/main/java/com/morninglock/
├── MainActivity.kt              # 设置界面
├── data/
│   ├── LockPreferences.kt      # SharedPreferences 封装
│   └── LockState.kt            # 锁定状态判断逻辑
├── overlay/
│   ├── CountdownFormatter.kt   # 倒计时格式化
│   └── LockOverlayManager.kt   # 悬浮窗管理
├── receiver/
│   ├── AlarmReceiver.kt        # 接收定时闹钟广播
│   ├── BootReceiver.kt         # 开机自启
│   └── ScreenUnlockReceiver.kt # 监听屏幕解锁
├── service/
│   └── LockService.kt          # 前台服务
└── util/
    ├── AlarmScheduler.kt       # AlarmManager 调度工具
    └── TimeUtils.kt            # 时间段判断工具
```

## License

MIT
