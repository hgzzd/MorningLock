---
时间: 2026-02-25 09:15
迭代的主题: 修复早晨首次解锁未触发锁机
迭代的细节: 定位并修复锁机触发链路问题，升级 LockService 动态广播注册以兼容 Android 13/14；新增服务启动时的兜底触发逻辑；统一解锁触发入口并补充触发/跳过原因日志；补充 LockState 相关单元测试场景（进行中锁定不重复触发）。
___
---
时间: 2026-02-26 12:22
迭代的主题: 修复时段内首次解锁未锁机并确保解锁后立即覆盖
迭代的细节: 移除 LockService 启动时的 service_create 自动触发，避免提前写入“今日已触发”导致首次解锁被跳过；将触发来源收敛为 unlock/app_resume，并在 MainActivity.onResume 增加补偿评估以覆盖部分机型漏发 ACTION_USER_PRESENT 的情况；触发状态写入与覆盖层展示绑定，展示失败时回滚 lastTriggeredTimestamp 与 lockStartTimestamp，防止未锁机却被记为已触发；执行 ./gradlew test 验证通过。
___
