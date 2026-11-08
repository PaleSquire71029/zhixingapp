# 实时 Backdrop 阶段 2 实施记录

> 对应专项计划：\`LIQUID_GLASS_REDESIGN_PLAN.md\`
> 对应 PR：#5
> 当前状态：代码已合并；Android CI 通过；真机验收待执行

## 实现概要

- 通过 \`rememberLayerBackdrop()\` 创建唯一的共享实时背景源。
- \`Modifier.layerBackdrop()\` 录制渐变底色和动态光斑。
- \`Modifier.drawBackdrop()\` 直接从同一层读取背景，替代每秒一次的 ImageBitmap 快照。
- 删除 \`toImageBitmap()\`、1 秒延时循环、快照计数和旧位图 Shader 绑定。
- 透明程度和背景模糊分别使用独立滑块。
- 保留轻微折射、边缘高光和触摸缩放反馈。

## 性能模式

| 模式 | 背景 | 模糊 | 折射 | 交互反馈 |
|---|---|---|---|---|
| Premium | 实时 | 较高 | 开 | 开 |
| Standard | 实时 | 适中 | 开 | 开 |
| Lite | 实时 | 关 | 关 | 关 |
| Off | 普通浅色面 | 关 | 关 | 关 |

## Android API 降级

- API 26–30：使用浅色透明表面，不启用模糊和折射。
- API 31–32：允许系统 RenderEffect 模糊，关闭折射。
- API 33+：允许模糊与 RuntimeShader lens 折射。

## CI 验收

- [ ] \`:app:compileDebugKotlin\`
- [ ] \`:app:testDebugUnitTest\`
- [ ] \`:app:assembleDebug\`

CI 通过只证明代码能编译和现有单元测试通过。动态背景是否丝滑、设备发热和滚动表现仍需目标真机验证。


## 本次 CI 结果与合并记录

- PR：[#5 实时 Backdrop 阶段 2](https://github.com/PaleSquire71029/zhixingapp/pull/5)，已于 2026-11-01 合并。
- 合并提交：`a71acf50a07e6a0c4d992e0e2f6664120a05cdd2`。
- Android Build：[#45 / Run ID 41256336581](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41256336581)，结果 success。
- 成功步骤：Android SDK 安装、资源完整性检查、Gradle 配置、Kotlin 编译、`:app:testDebugUnitTest`、`:app:assembleDebug`、`zhixingapp-debug` 临时 Artifact 上传。
- 临时 Artifact：ID `13063297144`，大小 11,248,442 bytes，SHA-256 `60a49cab67e1d5b87c534b84dcb1615c8bede9f0e76de53ae72d2d8c3dab64df`。
- APK 提交步骤因 PR 事件而跳过，没有更新 `artifacts/zhixingapp-preview.apk`，也没有生成新主分支预览包。

## 尚未验收

- [ ] 目标 Android 真机上观察动态光斑在玻璃内是否连续。
- [ ] 检查页面滚动、快速触摸、导航点击、横竖屏和系统 Insets。
- [ ] 分别测量性能档位的帧时间、温度、GPU/内存和耗电。
- [ ] 目标设备确认前，不将 Premium 设为默认性能承诺。

CI 成功不等于目标设备已通过流畅度和发热验收。下一阶段只建立独立的知行玻璃材质参数模型，不提前改动 MainActivity 底栏。
